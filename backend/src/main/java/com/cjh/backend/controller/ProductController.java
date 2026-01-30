package com.cjh.backend.controller;// 商品模块开始，新建 ProductController.java


import com.cjh.backend.dto.*;
import com.cjh.backend.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 发布商品
     */
    @PostMapping("/publish")
    public Result<Long> publishProduct(
            @RequestBody @Valid ProductPublishDto req,
            @CurrentUser Long userId) {
        log.info("用户 {} 发布商品：title = {}", userId, req.getTitle());
        try {
            Long productId = productService.publishProduct(userId, req);
            log.info("用户 {} 发布商品成功：productId = {}", userId, productId);
            return Result.success(productId, "发布成功");
        } catch (IllegalArgumentException e) {
            log.warn("用户 {} 发布商品失败：{}", userId, e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("用户 {} 发布商品异常", userId, e);
            return Result.fail("发布商品失败");
        }
    }


    /**
     * 修改商品状态（上架/下架等）
     */
    @PutMapping("/status")
    public Result<Void> updateProductStatus(
            @RequestBody @Valid ProductStatusUpdateDto req,
            @CurrentUser Long userId) {
        log.info("用户 {} 修改商品状态：productId = {}, newStatus = {}",
                userId, req.getId(), req.getStatus());
        try {
            boolean success = productService.updateProductStatus(userId, req);
            if (!success) {
                return Result.fail("操作失败，商品不存在、无权限或状态不允许变更");
            }
            log.info("用户 {} 修改商品状态成功：productId = {}, status = {}",
                    userId, req.getId(), req.getStatus());
            return Result.success(null, "状态修改成功");
        } catch (IllegalArgumentException e) {
            log.warn("用户 {} 修改商品状态失败：{}", userId, e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("用户 {} 修改商品状态异常", userId, e);
            return Result.fail("修改状态失败");
        }
    }

    /**
     * 删除商品（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(
            @PathVariable("id") Long productId,
            @CurrentUser Long userId) {
        log.info("用户 {} 尝试删除商品：productId = {}", userId, productId);
        try {
            boolean success = productService.deleteProduct(userId, productId);
            if (!success) {
                return Result.fail("删除失败，商品不存在或无权限");
            }
            log.info("用户 {} 删除商品成功：productId = {}", userId, productId);
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            log.error("用户 {} 删除商品异常", userId, e);
            return Result.fail("删除商品失败");
        }
    }

    /**
     * 查询我发布的商品列表
     */
    @GetMapping("/my")
    public Result<PageDto<ProductMyDto>> listMyProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @CurrentUser Long userId) {

        log.info("用户 {} 查询我的发布列表：page={}, size={}, status={}",
                userId, page, size, status);

        try {
            PageDto<ProductMyDto> result = productService.listMyProducts(userId, page, size, status);
            log.info("用户 {} 我的发布列表查询成功，共 {} 条", userId, result.getTotal());
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户 {} 查询我的发布列表异常", userId, e);
            return Result.fail("查询失败");
        }
    }

    /**
     * 商品搜索列表（买家浏览核心接口）
     */
    @GetMapping("/list")
    public Result<PageDto<ProductListDto>> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer condition,
            @RequestParam(required = false) String sort,          // 示例: price_asc, price_desc, time_desc
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("商品搜索请求：keyword={}, categoryId={}, page={}, size={}",
                keyword, categoryId, page, size);
        try {
            PageDto<ProductListDto> result = productService.listProducts(
                    keyword, categoryId, minPrice, maxPrice, condition, sort, page, size);

            log.info("商品搜索成功，共 {} 条", result.getTotal());
            return Result.success(result);
        } catch (Exception e) {
            log.error("商品搜索异常", e);
            return Result.fail("搜索失败");
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductDetailDto> getProductDetail(
            @PathVariable("id") Long productId,
            @CurrentUser Long currentUserId) {  // currentUserId 可为 null（未登录）

        log.info("查询商品详情：productId = {}, 当前用户 = {}", productId, currentUserId);

        try {
            ProductDetailDto detail = productService.getProductDetail(productId, currentUserId);
            if (detail == null) {
                return Result.fail(404, "商品不存在或已下架");
            }

            // 更新浏览量（可选异步）
            productService.incrementViewCount(productId);

            log.info("商品详情查询成功：productId = {}", productId);
            return Result.success(detail);
        } catch (Exception e) {
            log.error("查询商品详情异常：productId = {}", productId, e);
            return Result.fail("获取商品详情失败");
        }
    }
}