package com.cjh.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailDto {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private Integer conditionLevel;
    private Integer negotiable;
    private Integer viewCount;

    private ProductSellerDto seller;
    private List<ProductImageDto> images;
}
