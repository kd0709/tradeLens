package com.cjh.backend.service;

import com.cjh.backend.dto.Category.CategoryDto;
import com.cjh.backend.dto.Category.CategoryTreeDto;
import com.cjh.backend.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 45209
* @description 针对表【category(商品分类表)】的数据库操作Service
* @createDate 2026-01-29 18:58:49
*/
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树
     */
    List<CategoryTreeDto> getCategoryTree();

    /**
     * 获取分类列表（平铺）
     */
    List<CategoryDto> getCategoryList();
}
