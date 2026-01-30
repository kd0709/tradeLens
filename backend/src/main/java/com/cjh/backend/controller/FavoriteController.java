package com.cjh.backend.controller;


import com.cjh.backend.dto.Favorite.FavoriteListDto;
import com.cjh.backend.dto.Favorite.FavoriteToggleDto;
import com.cjh.backend.service.FavoriteService;
import com.cjh.backend.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cjh.backend.common.CurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    public Result<Boolean> toggleFavorite(
            @CurrentUser Long userId,
            @Valid @RequestBody FavoriteToggleDto dto) {
        log.info("用户 {} 切换收藏商品 {}", userId, dto.getProductId());
        try {
            boolean isFavorited = favoriteService.toggleFavorite(userId, dto);
            String msg = isFavorited ? "收藏成功" : "取消收藏成功";
            return Result.success(isFavorited, msg);
        } catch (Exception e) {
            log.error("用户 {} 切换收藏失败，商品ID: {}", userId, dto.getProductId(), e);
            return Result.fail("操作失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<FavoriteListDto>> listFavorites(@CurrentUser Long userId) {
        log.info("用户 {} 查询收藏列表", userId);
        try {
            List<FavoriteListDto> list = favoriteService.listFavorites(userId);
            log.info("用户 {} 收藏列表查询成功，共 {} 条", userId, list.size());
            return Result.success(list);
        } catch (Exception e) {
            log.error("用户 {} 查询收藏列表异常", userId, e);
            return Result.fail("获取收藏列表失败");
        }
    }
}