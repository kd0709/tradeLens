package com.cjh.backend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjh.backend.dto.SystemProductDto;
import com.cjh.backend.entity.Product;
import com.cjh.backend.service.ProductService;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理系统 - 商品模块 CRUD 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/product")
@RequiredArgsConstructor
public class SystemProductController {

    private final ProductService productService;

    /**
     * 1. 分页查询所有商品 (支持多条件筛选)
     */
    @GetMapping("/page")
    public Result<Page<SystemProductDto>> getProductPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer productStatus) {
        
        log.info("System Admin - 分页查询商品：page={}, size={}, keyword={}, status={}", page, size, keyword, productStatus);
        
        // 使用新的分页方法，包含主图信息
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SystemProductDto> resultPage = 
            productService.pageSystemProducts(keyword, productStatus, page, size);
        return Result.success(resultPage);
    }

    /**
     * 2. 根据 ID 查询单个商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable("id") Long id) {
        log.info("System Admin - 查询商品详情：id={}", id);
        Product product = productService.getById(id);
        if (product != null) {
            return Result.success(product);
        }
        return Result.fail(404, "商品不存在");
    }

    /**
     * 3. 新增商品 (后台管理员直接录入，非常规流程)
     */
    @PostMapping
    public Result<String> saveProduct(@RequestBody Product product) {
        log.info("System Admin - 新增商品：title={}", product.getTitle());
        boolean saved = productService.save(product);
        return saved ? Result.success("新增商品成功") : Result.fail("新增失败");
    }

    /**
     * 4. 修改商品 (可用于后台强行下架违规商品，即修改 productStatus = 3 或 1)
     */
    @PutMapping
    public Result<String> updateProduct(@RequestBody Product product) {
        if (product.getId() == null) {
            return Result.fail(400, "商品ID不能为空");
        }
        log.info("System Admin - 修改商品信息：id={}", product.getId());
        boolean updated = productService.updateById(product);
        return updated ? Result.success("修改商品成功") : Result.fail("修改失败，商品可能不存在");
    }

    /**
     * 5. 逻辑删除商品
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable("id") Long id) {
        log.info("System Admin - 删除商品：id={}", id);
        // 调用 removeById 会自动触发 MyBatis-Plus 的逻辑删除 (@TableLogic)
        boolean removed = productService.removeById(id);
        return removed ? Result.success("删除商品成功") : Result.fail("删除失败");
    }
}