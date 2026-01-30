package com.cjh.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductMyDto {
    private Long id;
    private String title;
    private BigDecimal price;
    private Integer productStatus;
    private String mainImage;  // 主图 URL
    private LocalDateTime createTime;
}


