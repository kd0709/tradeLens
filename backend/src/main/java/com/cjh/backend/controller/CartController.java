package com.cjh.backend.controller;

import com.cjh.backend.dto.Cart.AddToCartDto;
import com.cjh.backend.dto.Cart.CartListDto;
import com.cjh.backend.dto.Cart.UpdateCartQuantityDto;
import com.cjh.backend.service.CartService;
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


@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {


    private final CartService cartService;

    /**
     * 加入购物车
     */
    @PostMapping("/add")
    public Result<Long> addToCart(
            @CurrentUser Long userId,
            @Valid @RequestBody AddToCartDto dto) {
        log.info("用户 {} 加入购物车，商品ID: {}, 数量: {}", userId, dto.getProductId(), dto.getQuantity());
        try {
            Long cartId = cartService.addToCart(userId, dto);
            return Result.success(cartId, "加入购物车成功");
        } catch (Exception e) {
            log.error("用户 {} 加入购物车失败", userId, e);
            return Result.fail("加入购物车失败：" + e.getMessage());
        }
    }

    /**
     * 购物车列表
     */
    @GetMapping("/list")
    public Result<List<CartListDto>> listCart(@CurrentUser Long userId) {
        log.info("用户 {} 查询购物车列表", userId);
        try {
            List<CartListDto> list = cartService.listCart(userId);
            log.info("用户 {} 购物车查询成功，共 {} 条", userId, list.size());
            return Result.success(list);
        } catch (Exception e) {
            log.error("用户 {} 查询购物车异常", userId, e);
            return Result.fail("获取购物车列表失败");
        }
    }

    /**
     * 更新购物车数量
     */
    @PutMapping("/update")
    public Result<String> updateQuantity(
            @CurrentUser Long userId,
            @Valid @RequestBody UpdateCartQuantityDto dto) {
        log.info("用户 {} 更新购物车项 {} 数量为 {}", userId, dto.getId(), dto.getQuantity());
        try {
            cartService.updateQuantity(userId, dto);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("用户 {} 更新购物车数量失败", userId, e);
            return Result.fail("更新数量失败：" + e.getMessage());
        }
    }

    /**
     * 删除购物车项（支持批量）
     */
    @DeleteMapping("/delete")
    public Result<String> deleteCartItems(
            @CurrentUser Long userId,
            @RequestBody List<Long> cartIds) {
        log.info("用户 {} 删除购物车项: {}", userId, cartIds);
        try {
            cartService.deleteCartItems(userId, cartIds);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("用户 {} 删除购物车项失败", userId, e);
            return Result.fail("删除失败：" + e.getMessage());
        }
    }
}