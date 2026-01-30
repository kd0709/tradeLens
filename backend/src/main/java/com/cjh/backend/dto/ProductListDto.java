package com.cjh.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class ProductListDto {
    private Long id;
    private String title;
    private BigDecimal price;
    private Integer conditionLevel;
    private String mainImage;         // 主图
    private String sellerNickname;    // 卖家昵称
    private Integer viewCount;
    private LocalDateTime createTime;
}