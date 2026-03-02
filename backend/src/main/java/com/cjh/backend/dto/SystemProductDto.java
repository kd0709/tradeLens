package com.cjh.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 系统管理商品DTO，包含主图信息
 */
@Data
public class SystemProductDto {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer quantity; // 库存数量
    private Integer conditionLevel;
    private Integer negotiable;
    private Integer productStatus;
    private Integer isDeleted;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 主图URL
    private String mainImage;
}