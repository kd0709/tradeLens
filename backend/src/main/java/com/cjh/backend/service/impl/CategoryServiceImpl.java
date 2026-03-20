package com.cjh.backend.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.Category.CategoryDto;
import com.cjh.backend.dto.Category.CategoryTreeDto;
import com.cjh.backend.entity.Category;
import com.cjh.backend.service.CategoryService;
import com.cjh.backend.mapper.CategoryMapper;
import com.cjh.backend.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    // 注入我们写好的 Redis 工具类
    private final RedisUtils redisUtils;

    // 统一定义 Redis Key，防止硬编码写错
    private static final String CACHE_KEY_TREE = "tradelens:category:tree";
    private static final String CACHE_KEY_LIST = "tradelens:category:list";

    @Override
    public List<CategoryTreeDto> getCategoryTree() {
        // 1. 【性能优化】优先查询 Redis 缓存
        Object cachedObj = redisUtils.get(CACHE_KEY_TREE);
        if (cachedObj != null) {
            log.info("命中 Redis 分类树缓存");
            // 将 Redis 返回的 JSON 格式数据反序列化为具体的 DTO 集合
            return JSON.parseArray(JSON.toJSONString(cachedObj), CategoryTreeDto.class);
        }

        // 2. 缓存未命中，查询 MySQL
        log.info("未命中缓存，查询 MySQL 构建分类树");
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
                .orderByAsc(Category::getLevel, Category::getId);
        List<Category> categories = this.list(wrapper);

        // 转换 DTO
        List<CategoryTreeDto> allDtos = categories.stream().map(category -> {
            CategoryTreeDto dto = new CategoryTreeDto();
            BeanUtils.copyProperties(category, dto);
            dto.setChildren(new ArrayList<>());
            return dto;
        }).collect(Collectors.toList());

        // 3. 【算法优化】使用 Map 将组装树的时间复杂度从 O(n^2) 降至 O(n)
        Map<Long, CategoryTreeDto> dtoMap = allDtos.stream()
                .collect(Collectors.toMap(CategoryTreeDto::getId, dto -> dto));

        List<CategoryTreeDto> rootCategories = new ArrayList<>();
        for (CategoryTreeDto dto : allDtos) {
            if (dto.getParentId() == null || dto.getParentId() == 0) {
                rootCategories.add(dto);
            } else {
                // 直接从 Map 中 O(1) 获取父节点，取代原来的内层 for 循环
                CategoryTreeDto parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        // 4. 将构建好的树状结构存入 Redis (不设过期时间，依靠后台修改时主动清除)
        redisUtils.set(CACHE_KEY_TREE, rootCategories);

        return rootCategories;
    }

    @Override
    public List<CategoryDto> getCategoryList() {
        // 同理，列表也加上缓存
        Object cachedObj = redisUtils.get(CACHE_KEY_LIST);
        if (cachedObj != null) {
            return JSON.parseArray(JSON.toJSONString(cachedObj), CategoryDto.class);
        }

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
                .orderByAsc(Category::getLevel, Category::getId);
        List<Category> categories = this.list(wrapper);

        List<CategoryDto> dtoList = categories.stream().map(category -> {
            CategoryDto dto = new CategoryDto();
            BeanUtils.copyProperties(category, dto);
            return dto;
        }).collect(Collectors.toList());

        // 存入 Redis
        redisUtils.set(CACHE_KEY_LIST, dtoList);

        return dtoList;
    }

    // ==========================================================
    // 关键：重写 Mybatis-Plus 的增删改方法，保证缓存一致性！
    // ==========================================================

    @Override
    public boolean save(Category entity) {
        boolean result = super.save(entity);
        clearCategoryCache();
        return result;
    }

    @Override
    public boolean updateById(Category entity) {
        boolean result = super.updateById(entity);
        clearCategoryCache();
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        clearCategoryCache();
        return result;
    }

    /**
     * 清除分类相关的所有的 Redis 缓存
     */
    private void clearCategoryCache() {
        redisUtils.del(CACHE_KEY_TREE, CACHE_KEY_LIST);
        log.info("分类数据发生变更，已清除相关 Redis 缓存");
    }
}