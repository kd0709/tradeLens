package com.cjh.backend.dto.Product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// com.cjh.backend.dto.product.ProductDetailDto.java
@Data
public class ProductDetailDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private Integer conditionLevel;
    private Integer negotiable;
    private Integer productStatus;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 卖家信息
    private Long sellerId;
    private String sellerNickname;
    private String sellerAvatar;

    // 图片列表
    private List<String> images;

    // 当前用户是否收藏（未登录为 false）
    private Boolean isFavorited;

    // 可选：分类名称、卖家其他商品数等
}