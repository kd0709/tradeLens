package com.cjh.backend.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    // 注入 Redisson 客户端
    private final RedissonClient redissonClient;

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
        Set<Long> sellerIds = new HashSet<>();

        for (CreateOrderDto.OrderItemRequest itemReq : dto.getItems()) {
            // 定义针对单个商品的分布式锁 Key
            String lockKey = "lock:product:stock:" + itemReq.getProductId();
            RLock lock = redissonClient.getLock(lockKey);

            try {
                // 尝试获取锁：最多等待3秒，上锁后10秒自动解锁
                boolean isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
                if (!isLocked) {
                    throw new RuntimeException("系统繁忙，商品抢购人数过多，请稍后重试");
                }

                Product product = productMapper.selectById(itemReq.getProductId());
                if (product == null || product.getProductStatus() != 2) {
                    throw new RuntimeException("商品 " + (product != null ? product.getTitle() : "") + "已失效");
                }

                if (product.getUserId().equals(userId)) {
                    throw new RuntimeException("不能购买自己发布的商品");
                }

                if (!sellerIds.isEmpty() && !sellerIds.contains(product.getUserId())) {
                    throw new RuntimeException("不允许跨卖家合并结算，请选择同一卖家的商品进行结算");
                }
                sellerIds.add(product.getUserId());

                // 分布式锁内查库存
                Integer stock = productMapper.checkProductStock(itemReq.getProductId());
                if (stock == null || stock < itemReq.getQuantity()) {
                    throw new RuntimeException("商品 " + product.getTitle() + " 库存不足，当前库存: " + stock);
                }

                int affectedRows = productMapper.decreaseProductQuantity(itemReq.getProductId(), itemReq.getQuantity());
                if (affectedRows == 0) {
                    throw new RuntimeException("商品 " + product.getTitle() + " 扣减库存失败，请重试");
                }

                Integer remainingStock = productMapper.checkProductStock(itemReq.getProductId());
                if (remainingStock != null && remainingStock <= 0) {
                    Product pToUpdate = new Product();
                    pToUpdate.setId(itemReq.getProductId());
                    pToUpdate.setProductStatus(4);
                    productMapper.updateById(pToUpdate);
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

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("抢购请求被中断");
            } finally {
                // 释放锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
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
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new RuntimeException("订单状态不允许取消");
        }

        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
        for (OrderItem item : orderItems) {
            productMapper.increaseProductQuantity(item.getProductId(), item.getQuantity());
            Product product = productMapper.selectById(item.getProductId());
            if (product != null && product.getProductStatus() == 4) {
                int paidOrderItemCount = ordersMapper.countPaidOrdersByProductId(item.getProductId());
                if (paidOrderItemCount == 0) {
                    product.setProductStatus(2);
                    productMapper.updateById(product);
                }
            }
        }

        if (order.getStatus() == 2) {
            try {
                AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
                JSONObject bizContent = new JSONObject();
                bizContent.put("out_trade_no", orderNo);
                bizContent.put("refund_amount", order.getTotalPrice());
                bizContent.put("refund_reason", "用户主动取消订单");
                request.setBizContent(bizContent.toString());
                alipayClient.execute(request);
                log.info("订单 {} 已触发支付宝退款", orderNo);
            } catch (AlipayApiException e) {
                log.error("订单 {} 退款失败", orderNo, e);
                throw new RuntimeException("第三方退款调用失败，请稍后重试");
            }
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

    @Scheduled(fixedRate = 300000)
    @Transactional(rollbackFor = Exception.class)
    public void handleExpiredOrders() {
        log.info("开始处理超时未支付订单...");
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);
            LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Orders::getStatus, 1).lt(Orders::getCreateTime, expireTime);

            List<Orders> expiredOrders = ordersMapper.selectList(wrapper);
            if (expiredOrders.isEmpty()) return;

            int successCount = 0;
            for (Orders order : expiredOrders) {
                try {
                    List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
                    for (OrderItem item : orderItems) {
                        productMapper.increaseProductQuantity(item.getProductId(), item.getQuantity());
                        Product product = productMapper.selectById(item.getProductId());
                        if (product != null && product.getProductStatus() == 4) {
                            int paidOrderItemCount = ordersMapper.countPaidOrdersByProductId(item.getProductId());
                            if (paidOrderItemCount == 0) {
                                product.setProductStatus(2);
                                productMapper.updateById(product);
                            }
                        }
                    }
                    order.setStatus(5);
                    ordersMapper.updateById(order);
                    successCount++;
                } catch (Exception e) {
                    log.error("处理订单 {}超时取消失败", order.getOrderNo(), e);
                }
            }
            log.info("超时订单处理完成：成功处理 {} 个", successCount);
        } catch (Exception e) {
            log.error("处理超时订单任务异常", e);
        }
    }
}