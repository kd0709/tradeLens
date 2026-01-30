package com.cjh.backend.dto.Favorite;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteListDto {

    private Long id;                    // 收藏记录ID

    private Long productId;

    private String title;               // 商品标题

    private BigDecimal price;           // 商品价格

    private String imageUrl;            // 商品主图

    private LocalDateTime createTime;   // 收藏时间
}