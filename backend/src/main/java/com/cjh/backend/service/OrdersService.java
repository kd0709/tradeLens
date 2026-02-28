package com.cjh.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjh.backend.dto.Orders.CreateOrderDto;
import com.cjh.backend.dto.Orders.OrderDetailDto;
import com.cjh.backend.dto.Orders.OrderListDto;
import com.cjh.backend.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 45209
* @description 针对表【orders(订单表)】的数据库操作Service
* @createDate 2026-01-29 18:58:49
*/
public interface OrdersService extends IService<Orders> {

    OrderDetailDto createOrder(Long userId, CreateOrderDto dto);

    IPage<OrderListDto> listBuyerOrders(Page<Orders> page, Long buyerId, Integer status);

    IPage<OrderListDto> listSellerOrders(Page<Orders> page, Long sellerId, Integer status);

    OrderDetailDto getOrderDetail(Long userId, String orderNo);

    void cancelOrder(Long userId, String orderNo);

    void confirmReceipt(Long userId, String orderNo);

    void deliverOrder(Long sellerId, String orderNo, String trackingNo);

    String payOrder(Long userId, String orderNo, String payType);
}
