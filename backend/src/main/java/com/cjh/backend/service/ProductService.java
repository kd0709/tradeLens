package com.cjh.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjh.backend.dto.ProductDetailDto;
import com.cjh.backend.dto.ProductPublishDto;
import com.cjh.backend.dto.ProductQueryDto;
import com.cjh.backend.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;


/**
* @author 45209
* @description 针对表【product(商品表)】的数据库操作Service
* @createDate 2026-01-14 14:57:56
*/
public interface ProductService extends IService<Product> {

    Long publishProduct(Long userId, ProductPublishDto dto);

    void updateProductInfo(Long userId, Long productId, ProductPublishDto dto);

    void updateProductStatus(Long userId, Long productId, Integer status);

    void deleteProduct(Long userId, Long productId);

    ProductDetailDto getProductDetail(Long productId);

    IPage<ProductDetailDto> listProducts(ProductQueryDto query);

    IPage<ProductDetailDto> listProductsByCategory(Long categoryId, Integer page, Integer size);

    IPage<ProductDetailDto> listMyProducts(Long userId, Integer page, Integer size);

    void markProductSold(Long productId);

}
