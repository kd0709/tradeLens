package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.*;
import com.cjh.backend.entity.Product;
import com.cjh.backend.entity.ProductImage;
import com.cjh.backend.service.ProductService;
import com.cjh.backend.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


/**
* @author 45209
* @description 针对表【product(商品表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
    implements ProductService{

    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishProduct(Long userId, ProductPublishDto req) {
        // 1. 构建商品实体
        Product product = new Product();
        BeanUtils.copyProperties(req, product);
        product.setUserId(userId);

        // 2. 插入商品
        int rows = productMapper.insertProduct(product);
        if (rows == 0) {
            throw new IllegalStateException("发布失败");
        }
        Long productId = product.getId();

        // 3. 插入图片（如果有）
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            for (int i = 0; i < req.getImages().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(productId);
                image.setImageUrl(req.getImages().get(i));
                image.setIsMain(i == 0 ? 1 : 0);  // 第一张设为主图
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
        int rows = productMapper.deleteProductByIdAndUserId(productId, userId);

        // 可选：清理图片记录
        // productMapper.deleteProductImages(productId);

        return rows > 0;
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
        // 在 getProductDetail 方法中补充
        List<String> imageUrls = productMapper.getProductImageUrls(productId);
        detail.setImages(imageUrls);

        // 是否收藏（未登录返回 false）
        boolean isFavorited = false;
        if (currentUserId != null) {
            isFavorited = productMapper.isFavorited(currentUserId, productId);
        }
        detail.setIsFavorited(isFavorited);

        return detail;
    }

    @Override
    public void incrementViewCount(Long productId) {
        productMapper.incrementViewCount(productId);
    }
}




