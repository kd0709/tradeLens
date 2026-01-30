package com.cjh.backend.dto.Orders;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderDto {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    @NotNull(message = "地址ID不能为空")
    private Long addressId;
}