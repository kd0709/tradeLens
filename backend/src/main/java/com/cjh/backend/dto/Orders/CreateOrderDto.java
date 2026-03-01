package com.cjh.backend.dto.Orders;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDto {
    @NotNull(message = "地址ID不能为空")
    private Long addressId;

    // 新增：支持多商品列表
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemRequest> items;

    // 新增：需要清理的购物车ID集合
    private List<Long> cartIds;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}