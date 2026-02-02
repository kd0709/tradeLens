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


    /**
     * 创建订单
     */
    OrderDetailDto createOrder(Long userId, CreateOrderDto dto);

    /**
     * 买家订单列表
     */
    IPage<OrderListDto> listBuyerOrders(Page<Orders> page, Long buyerId, Integer status);

    /**
     * 卖家订单列表
     */
    IPage<OrderListDto> listSellerOrders(Page<Orders> page, Long sellerId, Integer status);

    /**
     * 订单详情
     */
    OrderDetailDto getOrderDetail(Long userId, String orderNo);

    /**
     * 取消订单（仅待支付）
     */
    void cancelOrder(Long userId, String orderNo);

    /**
     * 确认收货（仅已发货）
     */
    void confirmReceipt(Long userId, String orderNo);

    /**
     * 卖家发货
     */
    void deliverOrder(Long sellerId, String orderNo, String trackingNo);

    /**
     * 支付订单（模拟支付回调）
     */
    void payOrder(Long userId, String orderNo, String payType);
}
