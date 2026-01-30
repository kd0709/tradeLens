package com.cjh.backend.dto.Favorite;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteToggleDto {

    @NotNull(message = "商品ID不能为空")
    private Long productId;
}