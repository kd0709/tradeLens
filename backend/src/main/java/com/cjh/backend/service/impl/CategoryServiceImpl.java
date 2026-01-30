package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.entity.Category;
import com.cjh.backend.service.CategoryService;
import com.cjh.backend.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author 45209
* @description 针对表【category(商品分类表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

}




