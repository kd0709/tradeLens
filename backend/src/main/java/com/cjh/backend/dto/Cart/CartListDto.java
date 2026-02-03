package com.cjh.backend.dto.Cart;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartListDto {

    private Long id;               // 购物车项ID

    private Long productId;

    private String productTitle;   // 商品标题快照

    private BigDecimal price;      // 当前价格快照

    private Integer quantity;

    private String productImage;   // 主图

    private LocalDateTime createTime;
}