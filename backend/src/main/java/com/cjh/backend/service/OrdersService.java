package com.cjh.backend.service;

import com.cjh.backend.dto.OrderCreateDTO;
import com.cjh.backend.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 45209
* @description 针对表【orders(订单表)】的数据库操作Service
* @createDate 2026-01-18 22:32:09
*/
public interface OrdersService extends IService<Orders> {
    Long createOrder(Long buyerId, OrderCreateDTO dto);

    void payOrder(Long orderId, Long buyerId);

    void completeOrder(Long orderId, Long sellerId);

    void cancelOrder(Long orderId, Long buyerId);
}
