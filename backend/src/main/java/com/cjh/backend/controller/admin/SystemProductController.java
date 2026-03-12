package com.cjh.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjh.backend.dto.Product.AuditDto;
import com.cjh.backend.dto.Product.ProductExportDto;
import com.cjh.backend.dto.SystemProductDto;
import com.cjh.backend.entity.Product;
import com.cjh.backend.service.UserService;
import com.cjh.backend.service.impl.EmailService;
import com.cjh.backend.service.ProductService;
import com.cjh.backend.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理系统 - 商品模块 CRUD 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/product")
@RequiredArgsConstructor
public class SystemProductController {

    private final ProductService productService;
    private final EmailService emailService;
    private final UserService userService; // 注入 UserService



    /**
     * 1. 分页查询所有商品
     */
    @GetMapping("/page")
    public Result<Page<SystemProductDto>> getProductPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer productStatus) {

        log.info("System Admin - 分页查询商品：page={}, size={}, keyword={}, status={}", page, size, keyword, productStatus);
        Page<SystemProductDto> resultPage = productService.pageSystemProducts(keyword, productStatus, page, size);
        return Result.success(resultPage);
    }

    /**
     * 2. 根据 ID 查询单个商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable("id") Long id) {
        Product product = productService.getById(id);
        if (product != null) {
            return Result.success(product);
        }
        return Result.fail(404, "商品不存在");
    }

    /**
     * 3. 新增商品
     */
    @PostMapping
    public Result<String> saveProduct(@RequestBody Product product) {
        boolean saved = productService.save(product);
        return saved ? Result.success("新增商品成功") : Result.fail("新增失败");
    }

    /**
     * 4. 修改商品
     */
    @PutMapping
    public Result<String> updateProduct(@RequestBody Product product) {
        if (product.getId() == null) {
            return Result.fail(400, "商品ID不能为空");
        }
        boolean updated = productService.updateById(product);
        return updated ? Result.success("修改商品成功") : Result.fail("修改失败，商品可能不存在");
    }

    /**
     * 5. 逻辑删除商品
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable("id") Long id) {
        boolean removed = productService.removeById(id);
        return removed ? Result.success("删除商品成功") : Result.fail("删除失败");
    }

    /**
     * 6. 审核商品并触发邮件通知 (模块2功能)
     */
    @PutMapping("/audit")
    public Result<String> auditProduct(@RequestBody AuditDto auditDto) {
        Product product = productService.getById(auditDto.getProductId());
        if (product == null) {
            return Result.fail(404, "商品不存在");
        }

        if (auditDto.getPass()) {
            product.setProductStatus(2); // 上架
            if (productService.updateById(product)) {
                emailService.sendProductAuditPassEmail(product.getUserId(), product.getTitle());
                return Result.success("审核通过，已上架并通知用户");
            }
        } else {
            product.setProductStatus(3); // 拒绝/下架
            if (productService.updateById(product)) {
                // 【核心联动】：商品被违规驳回/下架，扣除卖家 5 分信用分！
                userService.changeCreditScore(product.getUserId(), -5, "商品违规被驳回或强制下架，商品ID: " + product.getId());
                return Result.success("审核已拒绝，并已扣除卖家信用分");
            }
        }
        return Result.fail("审核操作失败");
    }

    /**
     * 7. 获取下一条待审核商品 (模块4核心功能)
     */
    @GetMapping("/next-audit")
    public Result<Product> getNextAuditProduct() {
        log.info("System Admin - 获取下一条待审核商品");
        // 查询条件：状态为 1 (待审核)，按创建时间正序排队，取第一条
        Product nextProduct = productService.getOne(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getProductStatus, 1)
                        .orderByAsc(Product::getCreateTime)
                        .last("LIMIT 1")
        );

        if (nextProduct != null) {
            return Result.success(nextProduct);
        }
        return Result.fail(404, "暂无待审核商品");
    }

    /**
     * 8. 导出数据
     */
    @GetMapping("/export")
    public void exportProducts(HttpServletResponse response) {
        // 假设已创建 ProductExportDto
        List<ProductExportDto> exportList = productService.list().stream().map(p -> {
            ProductExportDto dto = new ProductExportDto();
            org.springframework.beans.BeanUtils.copyProperties(p, dto);
            return dto;
        }).collect(java.util.stream.Collectors.toList());

        com.cjh.backend.utils.ExcelUtils.export(response, "商品信息导出", "商品列表", exportList, ProductExportDto.class);
    }
}