package com.cjh.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductQueryDto {

    private Long categoryId;
    private String keyword;
    private Integer conditionLevel;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Integer page = 1;
    private Integer size = 10;
}
