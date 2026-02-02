package com.cjh.backend.controller;

import com.cjh.backend.dto.Category.CategoryDto;
import com.cjh.backend.dto.Category.CategoryTreeDto;
import com.cjh.backend.service.CategoryService;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    public Result<List<CategoryTreeDto>> getCategoryTree() {
        log.info("查询分类树");
        try {
            List<CategoryTreeDto> tree = categoryService.getCategoryTree();
            return Result.success(tree);
        } catch (Exception e) {
            log.error("查询分类树异常", e);
            return Result.fail("查询分类树失败");
        }
    }

    /**
     * 获取分类列表（平铺）
     */
    @GetMapping("/list")
    public Result<List<CategoryDto>> getCategoryList() {
        log.info("查询分类列表");
        try {
            List<CategoryDto> list = categoryService.getCategoryList();
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询分类列表异常", e);
            return Result.fail("查询分类列表失败");
        }
    }
}
