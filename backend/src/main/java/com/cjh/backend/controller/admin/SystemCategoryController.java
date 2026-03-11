package com.cjh.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjh.backend.dto.Category.CategoryExportDto;
import com.cjh.backend.entity.Category;
import com.cjh.backend.service.CategoryService;
import com.cjh.backend.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理系统 - 分类模块 CRUD 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/category")
@RequiredArgsConstructor
public class SystemCategoryController {

    private final CategoryService categoryService;

    /**
     * 1. 分页查询分类 (支持按名称模糊查询)
     */
    @GetMapping("/page")
    public Result<Page<Category>> getCategoryPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        
        log.info("System Admin - 分页查询分类：page={}, size={}, keyword={}", page, size, keyword);
        Page<Category> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Category::getName, keyword);
        }
        wrapper.orderByAsc(Category::getLevel).orderByAsc(Category::getId);
        
        return Result.success(categoryService.page(pageParam, wrapper));
    }

    /**
     * 2. 获取所有分类列表 (通常用于下拉框选择)
     */
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList() {
        log.info("System Admin - 获取所有分类列表");
        // 查询所有状态为正常(1)的分类
        List<Category> list = categoryService.list(
                new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1)
        );
        return Result.success(list);
    }

    /**
     * 3. 根据 ID 查询详情
     */
    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable("id") Long id) {
        log.info("System Admin - 查询分类详情：id={}", id);
        return Result.success(categoryService.getById(id));
    }

    /**
     * 4. 新增分类
     */
    @PostMapping
    public Result<String> saveCategory(@RequestBody Category category) {
        log.info("System Admin - 新增分类：name={}", category.getName());
        boolean saved = categoryService.save(category);
        return saved ? Result.success("新增分类成功") : Result.fail("新增分类失败");
    }

    /**
     * 5. 修改分类 (如修改名称、状态等)
     */
    @PutMapping
    public Result<String> updateCategory(@RequestBody Category category) {
        if (category.getId() == null) {
            return Result.fail(400, "分类ID不能为空");
        }
        log.info("System Admin - 修改分类：id={}", category.getId());
        boolean updated = categoryService.updateById(category);
        return updated ? Result.success("修改分类成功") : Result.fail("修改失败");
    }

    /**
     * 6. 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable("id") Long id) {
        log.info("System Admin - 删除分类：id={}", id);
        // 实际业务中可能需要判断该分类下是否有商品，这里做基础的删除处理
        boolean removed = categoryService.removeById(id);
        return removed ? Result.success("删除分类成功") : Result.fail("删除失败");
    }

    /**
     * 7. 导出数据
     */
    @GetMapping("/export")
    public void exportProducts(HttpServletResponse response) {
        // 假设已创建 ProductExportDto
        List<CategoryExportDto> exportList = categoryService.list().stream().map(p -> {
            CategoryExportDto dto = new CategoryExportDto();
            org.springframework.beans.BeanUtils.copyProperties(p, dto);
            return dto;
        }).collect(java.util.stream.Collectors.toList());

        com.cjh.backend.utils.ExcelUtils.export(response, "商品信息导出", "商品列表", exportList, CategoryExportDto.class);
    }
}