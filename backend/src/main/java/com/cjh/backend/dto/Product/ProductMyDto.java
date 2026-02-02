package com.cjh.backend.dto.Product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductMyDto {
    private Long id;
    private String title;
    private BigDecimal price;
    private Integer productStatus;
    private String mainImage;  // 主图 URL


    private LocalDateTime createTime;
}


