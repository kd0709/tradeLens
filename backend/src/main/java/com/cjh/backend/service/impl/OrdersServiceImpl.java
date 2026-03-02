package com.cjh.backend.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.config.AlipayConfig;
import com.cjh.backend.dto.Orders.CreateOrderDto;
import com.cjh.backend.dto.Orders.OrderDetailDto;
import com.cjh.backend.dto.Orders.OrderItemDto;
import com.cjh.backend.dto.Orders.OrderListDto;
import com.cjh.backend.entity.*;
import com.cjh.backend.mapper.*;
import com.cjh.backend.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailDto createOrder(Long userId, CreateOrderDto dto) {
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("收货地址异常");
        }

        Orders order = new Orders();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 20));
        order.setBuyerId(userId);
        order.setStatus(1);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        order.setCreateTime(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Set<Long> sellerIds = new HashSet<>(); // 用于检查是否跨卖家
        
        for (CreateOrderDto.OrderItemRequest itemReq : dto.getItems()) {
            Product product = productMapper.selectById(itemReq.getProductId());
            if (product == null || product.getProductStatus() != 2) {
                throw new RuntimeException("商品 " + (product != null ? product.getTitle() : "") + "已失效");
            }
        
            //检查是否跨卖家
            if (!sellerIds.isEmpty() && !sellerIds.contains(product.getUserId())) {
                throw new RuntimeException("不允许跨卖家合并结算，请选择同一卖家的商品进行结算");
            }
            sellerIds.add(product.getUserId());
        
            //检查库存是否充足并预扣库存
            Integer stock = productMapper.checkProductStock(itemReq.getProductId());
            if (stock == null || stock < itemReq.getQuantity()) {
                throw new RuntimeException("商品 " + product.getTitle() + "库不足，当前库存: " + stock + ", 请求数量: " + itemReq.getQuantity());
            }
                        
            //库存（防止并发超卖）
            int affectedRows = productMapper.decreaseProductQuantity(itemReq.getProductId(), itemReq.getQuantity());
            if (affectedRows == 0) {
                throw new RuntimeException("商品 " + product.getTitle() + "库扣减失败，请重试");
            }
        
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductTitle(product.getTitle());
            item.setPrice(product.getPrice());
            item.setQuantity(itemReq.getQuantity());
            item.setCreateTime(LocalDateTime.now());
            orderItems.add(item);
        
            if (order.getSellerId() == null) order.setSellerId(product.getUserId());
        }

        order.setTotalPrice(totalAmount);
        ordersMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        if (dto.getCartIds() != null && !dto.getCartIds().isEmpty()) {
            QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).in("id", dto.getCartIds());
            cartMapper.delete(queryWrapper);
        }

        return toDetailDto(order);
    }

    @Override
    public IPage<OrderListDto> listBuyerOrders(Page<Orders> page, Long buyerId, Integer status) {
        IPage<Orders> orderPage = ordersMapper.selectBuyerOrders(page, buyerId, status);
        return orderPage.convert(this::toListDto);
    }

    @Override
    public IPage<OrderListDto> listSellerOrders(Page<Orders> page, Long sellerId, Integer status) {
        IPage<Orders> orderPage = ordersMapper.selectSellerOrders(page, sellerId, status);
        return orderPage.convert(this::toListDto);
    }

    @Override
    public OrderDetailDto getOrderDetail(Long userId, String orderNo) {
        Orders order = ordersMapper.selectByOrderNo(orderNo);
        if (order == null || (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId))) {
            throw new RuntimeException("订单不存在或无权限查看");
        }

        OrderDetailDto dto = toDetailDto(order);

        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        List<OrderItemDto> itemDtos = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemDto itemDto = new OrderItemDto();
            BeanUtils.copyProperties(item, itemDto);
            itemDtos.add(itemDto);
        }
        dto.setItems(itemDtos);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, String orderNo) {
        Orders order = ordersMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new RuntimeException("订单不存在或无权限");
        }
        // 检查订单状态是否允许取消
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new RuntimeException("订单状态不允许取消");
        }
        
        // 如果订单已支付但未发货，则需要回滚库存和处理退款
        if (order.getStatus() == 2) {  //已支付状态
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
            for (OrderItem item : orderItems) {
                //回滚库存
                productMapper.increaseProductQuantity(item.getProductId(), item.getQuantity());
                        
                // 如果商品状态已经是已售(4)，需要将其改回上架(2)
                Product product = productMapper.selectById(item.getProductId());
                if (product != null && product.getProductStatus() == 4) { // 4 = 已售
                    // 检查是否还有其他已支付的订单使用该商品
                    // 如果没有其他已支付订单占用库存，则可以恢复为上架状态
                    int paidOrderItemCount = ordersMapper.countPaidOrdersByProductId(item.getProductId());
                    if (paidOrderItemCount == 0) {
                        product.setProductStatus(2); // 2 = 上架
                        productMapper.updateById(product);
                    }
                }
            }
                    
            // TODO:这里应该调用支付宝退款接口
            // alipayClient.execute(refundRequest);
            //暂记录日志，实际项目中需要接入支付宝退款API
            log.info("订单 {}已支付，需要执行退款操作", orderNo);
        }

        order.setStatus(5);
        int rows = ordersMapper.updateById(order);
        if (rows == 0) {
            throw new IllegalStateException("取消订单失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long userId, String orderNo) {
        Orders order = ordersMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new RuntimeException("订单不存在或无权限");
        }
        if (order.getStatus() != 3) {
            throw new RuntimeException("订单状态不允许确认收货");
        }

        order.setStatus(4);
        order.setFinishTime(LocalDateTime.now());
        
        //更新订单相关商品状态（根据库存判断）
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                //检查商品是否还有库存
                Integer currentStock = productMapper.checkProductStock(item.getProductId());
                if (currentStock == null || currentStock <= 0) {
                    //没有库存了，标记为已售
                    product.setProductStatus(4); // 4 =已售
                } else {
                    //还有库存，保持上架状态
                    product.setProductStatus(2); // 2 =上架
                }
                productMapper.updateById(product);
            }
        }
        
        int rows = ordersMapper.updateById(order);
        if (rows == 0) {
            throw new IllegalStateException("确认收货失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverOrder(Long sellerId, String orderNo, String trackingNo) {
        Orders order = ordersMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getSellerId().equals(sellerId)) {
            throw new RuntimeException("订单不存在或无权限");
        }
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不允许发货");
        }

        order.setStatus(3);
        order.setDeliveryTime(LocalDateTime.now());
        order.setTrackingNo(trackingNo);
        int rows = ordersMapper.updateById(order);
        if (rows == 0) {
            throw new IllegalStateException("发货失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String payOrder(Long userId, String orderNo, String payType) {
        Orders order = ordersMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new RuntimeException("订单不存在或无权限");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不允许支付");
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", order.getOrderNo());
        bizContent.put("total_amount", order.getTotalPrice());
        bizContent.put("subject", "TradeLens订单支付:" + order.getOrderNo());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        // 注意：库存扣减将在支付宝异步通知中处理
        order.setStatus(1); // 保持待支付状态直到收到支付宝通知
        ordersMapper.updateById(order);

        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝跳转失败：" + e.getMessage());
        }
    }

    private OrderListDto toListDto(Orders order) {
        OrderListDto dto = new OrderListDto();
        BeanUtils.copyProperties(order, dto);
        return dto;
    }

    private OrderDetailDto toDetailDto(Orders order) {
        OrderDetailDto dto = new OrderDetailDto();
        BeanUtils.copyProperties(order, dto);
        return dto;
    }

    /**
     *定：处理超时未支付的订单
     *分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟执行一次
    @Transactional(rollbackFor = Exception.class)
    public void handleExpiredOrders() {
        log.info("开始处理超时未支付订单...");

        try {
            // 查询30分钟前创建的待支付订单
            LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);
            LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Orders::getStatus, 1) //待状态
                   .lt(Orders::getCreateTime, expireTime);
            
            List<Orders> expiredOrders = ordersMapper.selectList(wrapper);
            
            if (expiredOrders.isEmpty()) {
                log.info("没有发现超时未支付订单");
                return;
            }
            
            log.info("发现 {} 个超时未支付订单，开始处理...", expiredOrders.size());
            
            int successCount = 0;
            for (Orders order : expiredOrders) {
                try {
                    //回滚库存
                    List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
                    for (OrderItem item : orderItems) {
                        productMapper.increaseProductQuantity(item.getProductId(), item.getQuantity());
                    }
                    
                    // 更新订单状态为已取消
                    order.setStatus(5);
                    ordersMapper.updateById(order);
                    
                    successCount++;
                    log.info("订单 {}超自动取消成功", order.getOrderNo());
                } catch (Exception e) {
                    log.error("处理订单 {}超取消失败", order.getOrderNo(), e);
                }
            }
            
            log.info("超时订单处理完成：成功处理 {} 个，失败 {} 个", 
                    successCount, expiredOrders.size() - successCount);
                    
        } catch (Exception e) {
            log.error("处理超时订单任务异常", e);
        }
    }
}