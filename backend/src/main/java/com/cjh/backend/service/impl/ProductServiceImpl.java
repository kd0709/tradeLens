package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.*;
import com.cjh.backend.dto.Product.*;
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

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
    implements ProductService{

    private final ProductMapper productMapper;

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
}