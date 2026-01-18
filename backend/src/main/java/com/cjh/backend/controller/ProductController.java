package com.cjh.backend.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjh.backend.dto.ProductDetailDto;
import com.cjh.backend.dto.ProductPublishDto;
import com.cjh.backend.dto.ProductQueryDto;
import com.cjh.backend.dto.ProductStatusDto;
import com.cjh.backend.service.ProductService;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cjh.backend.common.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 发布商品
     */
    @PostMapping("/publish")
    public Result<Long> publish(
            @CurrentUser Long userId,
            @Valid @RequestBody ProductPublishDto dto) {

        Long productId = productService.publishProduct(userId, dto);
        return Result.success(productId);
    }

    /**
     * 编辑商品
     */
    @PutMapping("/changeInfo/{productId}")
    public Result<Void> changeInfo(
            @CurrentUser Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductPublishDto dto) {

        productService.updateProductInfo(userId, productId, dto);
        return Result.success();
    }

    /**
     * 商品上下架
     */
    @PutMapping("/changeStatus/{productId}")
    public Result<Void> changeStatus(
            @CurrentUser Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductStatusDto dto) {

        productService.updateProductStatus(userId, productId, dto.getStatus());
        return Result.success();
    }

    /**
     * 删除商品（逻辑删除）
     */
    @DeleteMapping("/delete/{productId}")
    public Result<Void> delete(
            @CurrentUser Long userId,
            @PathVariable Long productId) {

        productService.deleteProduct(userId, productId);
        return Result.success();
    }

    /**
     * 商品详情
     */
    @GetMapping("/detail/{productId}")
    public Result<ProductDetailDto> detail(@PathVariable Long productId) {

        ProductDetailDto detail = productService.getProductDetail(productId);
        return Result.success(detail);
    }

    /**
     * 商品列表（分页 + 条件）
     */
    @GetMapping("/list")
    public Result<IPage<ProductDetailDto>> list(ProductQueryDto queryDTO) {

        IPage<ProductDetailDto> page = productService.listProducts(queryDTO);
        return Result.success(page);
    }

    /**
     * 分类商品列表（包含子分类）
     */
    @GetMapping("/listCategory/{categoryId}")
    public Result<IPage<ProductDetailDto>> listByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        IPage<ProductDetailDto> result =
                productService.listProductsByCategory(categoryId, page, size);
        return Result.success(result);
    }

    /**
     * 我的商品列表
     */
    @GetMapping("/listMy")
    public Result<IPage<ProductDetailDto>> myProducts(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        IPage<ProductDetailDto> result =
                productService.listMyProducts(userId, page, size);
        return Result.success(result);
    }

    /**
     * 标记商品已售（订单完成后调用）
     */
    @PutMapping("/sold/{productId}")
    public Result<Void> markSold(@PathVariable Long productId) {

        productService.markProductSold(productId);
        return Result.success();
    }
}
