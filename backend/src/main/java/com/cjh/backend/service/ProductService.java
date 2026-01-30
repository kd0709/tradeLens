package com.cjh.backend.service;

import com.cjh.backend.dto.*;
import com.cjh.backend.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
* @author 45209
* @description 针对表【product(商品表)】的数据库操作Service
* @createDate 2026-01-29 18:58:49
*/
public interface ProductService extends IService<Product> {

    Long publishProduct(Long userId, ProductPublishDto req);

    boolean updateProductStatus(Long userId, ProductStatusUpdateDto req);

    boolean deleteProduct(Long userId, Long productId);

    PageDto<ProductMyDto> listMyProducts(Long userId, Integer page, Integer size, Integer status);

    PageDto<ProductListDto> listProducts(
            String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Integer condition, String sort, Integer page, Integer size);

    ProductDetailDto getProductDetail(Long productId, Long currentUserId);

    void incrementViewCount(Long productId);

}
