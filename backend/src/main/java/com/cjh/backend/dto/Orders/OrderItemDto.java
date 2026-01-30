package com.cjh.backend.dto.Orders;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {

    private Long id;

    private Long productId;

    private String productTitle;

    private BigDecimal price;

    private Integer quantity;
}