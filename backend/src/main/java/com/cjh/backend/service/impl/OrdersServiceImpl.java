package com.cjh.backend.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        for (CreateOrderDto.OrderItemRequest itemReq : dto.getItems()) {
            Product product = productMapper.selectById(itemReq.getProductId());
            if (product == null || product.getProductStatus() != 2) {
                throw new RuntimeException("商品 " + (product != null ? product.getTitle() : "") + " 已失效");
            }

            // 检查库存是否充足
            int stock = productMapper.checkProductStock(itemReq.getProductId());
            if (stock < itemReq.getQuantity()) {
                throw new RuntimeException("商品 " + product.getTitle() + " 库存不足，当前库存: " + stock + ", 请求数量: " + itemReq.getQuantity());
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
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不允许取消");
        }

        // 如果订单已支付但未发货，则需要回滚库存
        if (order.getStatus() == 2) {  // 已支付状态
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
            for (OrderItem item : orderItems) {
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
        
        // 更新订单相关商品状态为已售
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setProductStatus(4); // 4 = 已售
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
}