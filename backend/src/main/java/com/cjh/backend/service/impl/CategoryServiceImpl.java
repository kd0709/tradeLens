package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.Category.CategoryDto;
import com.cjh.backend.dto.Category.CategoryTreeDto;
import com.cjh.backend.entity.Category;
import com.cjh.backend.service.CategoryService;
import com.cjh.backend.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 45209
* @description 针对表【category(商品分类表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

    @Override
    public List<CategoryTreeDto> getCategoryTree() {
        // 查询所有启用的分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
               .orderByAsc(Category::getLevel, Category::getId);
        List<Category> categories = this.list(wrapper);

        // 转换为DTO
        List<CategoryTreeDto> allDtos = categories.stream().map(category -> {
            CategoryTreeDto dto = new CategoryTreeDto();
            BeanUtils.copyProperties(category, dto);
            dto.setChildren(new ArrayList<>());
            return dto;
        }).collect(Collectors.toList());

        // 构建树形结构
        List<CategoryTreeDto> rootCategories = new ArrayList<>();
        for (CategoryTreeDto dto : allDtos) {
            if (dto.getParentId() == null) {
                // 一级分类
                rootCategories.add(dto);
            } else {
                // 找到父分类并添加到children
                for (CategoryTreeDto parent : allDtos) {
                    if (parent.getId().equals(dto.getParentId())) {
                        parent.getChildren().add(dto);
                        break;
                    }
                }
            }
        }

        return rootCategories;
    }

    @Override
    public List<CategoryDto> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
               .orderByAsc(Category::getLevel, Category::getId);
        List<Category> categories = this.list(wrapper);

        return categories.stream().map(category -> {
            CategoryDto dto = new CategoryDto();
            BeanUtils.copyProperties(category, dto);
            return dto;
        }).collect(Collectors.toList());
    }
}




