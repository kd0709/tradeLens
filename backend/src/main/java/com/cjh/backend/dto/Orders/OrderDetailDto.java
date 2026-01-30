package com.cjh.backend.dto.Orders;

import lombok.Data;

import java.util.List;

@Data
public class OrderDetailDto extends OrderListDto {

    private List<OrderItemDto> items;  // 订单明细
}