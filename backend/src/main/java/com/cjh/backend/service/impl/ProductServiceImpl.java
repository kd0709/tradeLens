package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.*;
import com.cjh.backend.dto.Product.*;
import com.cjh.backend.entity.Favorite;
import com.cjh.backend.entity.Product;
import com.cjh.backend.entity.ProductImage;
import com.cjh.backend.entity.User;
import com.cjh.backend.service.FavoriteService;
import com.cjh.backend.service.ProductService;
import com.cjh.backend.mapper.ProductMapper;
import com.cjh.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
    implements ProductService{

    private final ProductMapper productMapper;
    private final FavoriteService favoriteService;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishProduct(Long userId, ProductPublishDto req) {
        Product product = new Product();
        BeanUtils.copyProperties(req, product);
        product.setUserId(userId);

        int rows = productMapper.insertProduct(product);
        if (rows == 0) {
            throw new IllegalStateException("发布失败");
        }
        Long productId = product.getId();

        if (req.getImages() != null && !req.getImages().isEmpty()) {
            for (int i = 0; i < req.getImages().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(productId);
                image.setImageUrl(req.getImages().get(i));
                image.setIsMain(i == 0 ? 1 : 0);
                image.setSort(i);
                productMapper.insertProductImage(image);
            }
        }
        return productId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProductStatus(Long userId, ProductStatusUpdateDto req) {
        int rows = productMapper.updateProductStatus(
                req.getId(),
                userId,
                req.getStatus()
        );
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(Long userId, Long productId) {
        return productMapper.deleteProductByIdAndUserId(productId, userId) > 0;
    }

    @Override
    public PageDto<ProductMyDto> listMyProducts(Long userId, Integer page, Integer size, Integer status) {
        int offset = (page - 1) * size;

        List<ProductMyDto> list = productMapper.listMyProducts(userId, status, offset, size);
        int total = productMapper.countMyProducts(userId, status);

        PageDto<ProductMyDto> pageDto = new PageDto<>();
        pageDto.setList(list);
        pageDto.setTotal((long) total);
        pageDto.setPage(page);
        pageDto.setSize(size);

        return pageDto;
    }

    @Override
    public PageDto<ProductListDto> listProducts(
            String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Integer condition, String sort, Integer page, Integer size) {

        int currentPage = Math.max(1, page);
        int pageSize = Math.max(1, Math.min(size, 50));
        int offset = (currentPage - 1) * pageSize;

        List<ProductListDto> list = productMapper.searchProducts(
                keyword, categoryId, minPrice, maxPrice, condition, sort, offset, pageSize);

        int total = productMapper.countSearchProducts(
                keyword, categoryId, minPrice, maxPrice, condition);

        PageDto<ProductListDto> pageDto = new PageDto<>();
        pageDto.setList(list);
        pageDto.setTotal((long) total);
        pageDto.setPage(currentPage);
        pageDto.setSize(pageSize);

        return pageDto;
    }

    @Override
    public ProductDetailDto getProductDetail(Long productId, Long currentUserId) {
        ProductDetailDto detail = productMapper.getProductDetailById(productId);
        if (detail == null) {
            return null;
        }

        List<String> imageUrls = productMapper.getProductImageUrls(productId);
        detail.setImages(imageUrls);

        boolean isFavorited = currentUserId != null && productMapper.isFavorited(currentUserId, productId);
        detail.setIsFavorited(isFavorited);

        return detail;
    }

    @Override
    public void incrementViewCount(Long productId) {
        productMapper.incrementViewCount(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(Long userId, ProductUpdateDto req) {
        Product existingProduct = productMapper.selectById(req.getId());
        if (existingProduct == null || !existingProduct.getUserId().equals(userId)) {
            return false;
        }

        if (existingProduct.getProductStatus() != 1 && existingProduct.getProductStatus() != 2) {
            throw new IllegalArgumentException("商品状态不允许编辑");
        }

        Product product = new Product();
        BeanUtils.copyProperties(req, product);
        product.setId(req.getId());
        product.setUserId(null);
        product.setProductStatus(null);
        product.setViewCount(null);
        product.setCreateTime(null);

        int rows = productMapper.updateById(product);
        if (rows == 0) {
            return false;
        }

        if (req.getImages() != null) {
            productMapper.deleteProductImages(req.getId());
            for (int i = 0; i < req.getImages().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(req.getId());
                image.setImageUrl(req.getImages().get(i));
                image.setIsMain(i == 0 ? 1 : 0);
                image.setSort(i);
                productMapper.insertProductImage(image);
            }
        }

        return true;
    }

    @Override
    public Page<SystemProductDto> pageSystemProducts(String keyword, Integer productStatus, Integer page, Integer size) {
        int offset = (page - 1) * size;

        List<SystemProductDto> list = productMapper.selectSystemProductPage(keyword, productStatus, offset, size);
        int total = productMapper.countSystemProducts(keyword, productStatus);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SystemProductDto> resultPage = new Page<>();
        resultPage.setRecords(list);
        resultPage.setTotal(total);
        resultPage.setCurrent(page);
        resultPage.setSize(size);

        return resultPage;
    }

    /**
     * 获取个性化推荐商品列表
     */

    @Override
    public Page<ProductListDto> getRecommendProducts(Long userId, Integer page, Integer size) {
        // 1. 获取基础在售商品库 (status = 2 代表上架)
        List<Product> allActiveProducts = this.list(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getProductStatus, 2)
                        .eq(Product::getIsDeleted, 0)
        );

        // 2. 提取用户偏好画像 (如果用户未登录，偏好集合为空)
        Set<Long> preferCategoryIds = new HashSet<>();
        if (userId != null) {
            List<Favorite> favorites = favoriteService.list(
                    new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId)
            );
            if (!favorites.isEmpty()) {
                List<Long> favProductIds = favorites.stream().map(Favorite::getProductId).collect(Collectors.toList());
                List<Product> favProducts = this.listByIds(favProductIds);
                preferCategoryIds = favProducts.stream().map(Product::getCategoryId).collect(Collectors.toSet());
            }
        }

        // 3. 批量获取卖家信息，用于读取信用分
        Set<Long> sellerIds = allActiveProducts.stream().map(Product::getUserId).collect(Collectors.toSet());
        Map<Long, User> sellerMap = new HashMap<>();
        if (!sellerIds.isEmpty()) {
            List<User> sellers = userService.listByIds(sellerIds);
            sellerMap = sellers.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        // 4. 综合权重计算打分
        final Set<Long> finalPreferCategoryIds = preferCategoryIds;
        final Map<Long, User> finalSellerMap = sellerMap;

        allActiveProducts.sort((p1, p2) -> {
            double score1 = calculateProductScore(p1, finalPreferCategoryIds, finalSellerMap.get(p1.getUserId()));
            double score2 = calculateProductScore(p2, finalPreferCategoryIds, finalSellerMap.get(p2.getUserId()));
            // 降序排列 (分数高的在前面)
            return Double.compare(score2, score1);
        });

        // 5. 内存分页并转为 DTO 格式返回
        int total = allActiveProducts.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<Product> pagedList = start >= total ? Collections.emptyList() : allActiveProducts.subList(start, end);

        List<ProductListDto> dtoList = pagedList.stream().map(p -> {
            ProductListDto dto = new ProductListDto();
            dto.setId(p.getId());
            dto.setTitle(p.getTitle());
            dto.setPrice(p.getPrice());
            dto.setConditionLevel(p.getConditionLevel());
            dto.setViewCount(p.getViewCount());
            // 这里根据实际 DTO 结构赋值主图等字段
            return dto;
        }).collect(Collectors.toList());

        Page<ProductListDto> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    /**
     * 核心打分算法
     */
    private double calculateProductScore(Product product, Set<Long> preferCategoryIds, User seller) {
        double score = 0.0;

        // a. 偏好匹配权重 (匹配到感兴趣的分类直接加 50 分)
        if (preferCategoryIds.contains(product.getCategoryId())) {
            score += 50.0;
        }

        // b. 卖家信用权重 (基础分100，每高1分算作0.5权重)
        if (seller != null && seller.getCreditScore() != null) {
            score += (seller.getCreditScore() - 100) * 0.5;
        }

        // c. 商品成色权重 (全新+10分，几乎全新+5分)
        if (product.getConditionLevel() == 1) score += 10.0;
        else if (product.getConditionLevel() == 2) score += 5.0;

        // d. 热度权重 (浏览量 / 10)
        if (product.getViewCount() != null) {
            score += product.getViewCount() * 0.1;
        }

        return score;
    }

}