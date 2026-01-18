package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.*;
import com.cjh.backend.entity.Product;
import com.cjh.backend.entity.ProductImage;
import com.cjh.backend.mapper.CategoryMapper;
import com.cjh.backend.mapper.UserMapper;
import com.cjh.backend.mapper.ProductImageMapper;
import com.cjh.backend.service.ProductService;
import com.cjh.backend.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author 45209
* @description 针对表【product(商品表)】的数据库操作Service实现
* @createDate 2026-01-14 14:57:56
*/
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
    implements ProductService{

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
//    private final ProductImageMapper productImageMapper;




    @Override
    @Transactional
    public Long publishProduct(Long userId, ProductPublishDto dto) {

        // 1. 构建商品实体
        Product product = new Product();
        product.setUserId(userId);
        product.setCategoryId(dto.getCategoryId());
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setConditionLevel(dto.getConditionLevel());
        product.setNegotiable(
                dto.getNegotiable() == null ? 1 : dto.getNegotiable()
        );
        product.setProductStatus(1); // 待审核
        product.setIsDeleted(0);
        product.setViewCount(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());

        // 2. 插入商品主表
        int rows = productMapper.insert(product);
        if (rows != 1) {
//            throw new BusinessException("商品发布失败");
        }

        Long productId = product.getId();

//        // 3. 保存商品图片
//        List<ProductImageDto> images = dto.getImages();
//        for (ProductImage image : images) {
//            image.setProductId(productId);
//            image.setCreateTime(LocalDateTime.now());
//        }
//        productImageMapper.insertBatch(images);

        return productId;
    }

    @Override
    @Transactional
    public void updateProductInfo(Long userId, Long productId, ProductPublishDto dto) {
        // 1. 校验商品归属
        Product product =
                productMapper.selectByIdAndUser(productId, userId);

        if (product == null) {
//            throw new BusinessException("商品不存在或无权操作");
        }

        // 2. 校验状态
        Integer status = product.getProductStatus();
        if (status != 1 && status != 3) {
//            throw new BusinessException("当前状态不允许修改商品信息");
        }

        // 3. 更新商品信息
        product.setCategoryId(dto.getCategoryId());
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setConditionLevel(dto.getConditionLevel());
        product.setNegotiable(
                dto.getNegotiable() == null ? 1 : dto.getNegotiable()
        );
        product.setProductStatus(1); // 重新审核
        product.setUpdateTime(LocalDateTime.now());

        productMapper.updateProductInfo(product);

        // 4. 更新图片（先删后插）
        productImageMapper.deleteByProductId(productId);

//        for (ProductImageDto image : dto.getImages()) {
//            image.setProductId(productId);
//            image.setCreateTime(LocalDateTime.now());
//        }
//        productImageMapper.insertBatch(dto.getImages());
    }

    @Override
    @Transactional
    public void updateProductStatus(Long userId, Long productId, Integer status) {
        if (status == null || (status != 2 && status != 3)) {
//            throw new BusinessException("非法的商品状态");
        }

        // 1. 校验商品归属
        Product product =
                productMapper.selectByIdAndUser(productId, userId);

        if (product == null) {
//            throw new BusinessException("商品不存在或无权操作");
        }

        Integer currentStatus = product.getProductStatus();

        // 2. 状态流转校验
        if (currentStatus == 4) {
//            throw new BusinessException("已售商品不可修改状态");
        }

        if (currentStatus == 1) {
//            throw new BusinessException("待审核商品不可修改状态");
        }

        if (currentStatus.equals(status)) {
            return; // 状态相同，直接忽略
        }

        // 3. 执行更新
        productMapper.updateProductStatus(productId, status);
    }

    @Override
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Product product =
                productMapper.selectByIdAndUser(productId, userId);

        if (product == null) {
//            throw new BusinessException("商品不存在或无权操作");
        }

        if (product.getProductStatus() == 2) {
//            throw new BusinessException("上架商品不可删除");
        }

        if (product.getIsDeleted() == 1) {
            return; // 已删除，忽略
        }

        productMapper.logicalDelete(productId);
    }

    @Override
    @Transactional
    public ProductDetailDto getProductDetail(Long productId) {

        // 1. 查询商品
        Product product = productMapper.selectByIdAndVisible(productId);
        if (product == null) {
//            throw new BusinessException("商品不存在或已下架");
        }

        // 2. 浏览量 +1
        productMapper.incrementViewCount(productId);

        // 3. 查询图片
        List<ProductImageDto> images =
                productImageMapper.selectByProductId(productId);

        // 4. 查询卖家
        ProductSellerDto seller =
                userMapper.selectSellerById(product.getUserId());

        // 5. 组装 VO
        ProductDetailDto detail = new ProductDetailDto();
        detail.setId(product.getId());
        detail.setTitle(product.getTitle());
        detail.setDescription(product.getDescription());
        detail.setPrice(product.getPrice());
        detail.setQuantity(product.getQuantity());
        detail.setConditionLevel(product.getConditionLevel());
        detail.setNegotiable(product.getNegotiable());
        detail.setViewCount(product.getViewCount() + 1);

        detail.setImages(images);
        detail.setSeller(seller);

        return detail;
    }

    @Override
    @Transactional
    public IPage<ProductDetailDto> listProducts(ProductQueryDto query) {

        Page<Product> page = new Page<>(
                query.getPage(),
                query.getSize()
        );

        IPage<Product> productPage =
                productMapper.selectPageByQuery(page, query);

        Page<ProductDetailDto> result = new Page<>();
        result.setCurrent(productPage.getCurrent());
        result.setSize(productPage.getSize());
        result.setTotal(productPage.getTotal());

        List<ProductDetailDto> records = productPage.getRecords()
                .stream()
                .map(this::toSimpleDetail)
                .toList();

        result.setRecords(records);
        return result;
    }

    @Override
    @Transactional
    public IPage<ProductDetailDto> listProductsByCategory(Long categoryId, Integer page, Integer size) {
        if (categoryId == null) {
//            throw new BusinessException("分类ID不能为空");
        }

        ProductQueryDto query = new ProductQueryDto();
        query.setCategoryId(categoryId);
        query.setPage(page == null ? 1 : page);
        query.setSize(size == null ? 10 : size);

        return this.listProducts(query);
    }

    @Override
    @Transactional
    public IPage<ProductDetailDto> listMyProducts(Long userId, Integer page, Integer size) {
        Page<Product> mpPage = new Page<>(
                page == null ? 1 : page,
                size == null ? 10 : size
        );

        IPage<Product> productPage =
                productMapper.selectMyProducts(mpPage, userId);

        Page<ProductDetailDto> result = new Page<>();
        result.setCurrent(productPage.getCurrent());
        result.setSize(productPage.getSize());
        result.setTotal(productPage.getTotal());

        List<ProductDetailDto> records = productPage.getRecords()
                .stream()
                .map(this::toSimpleDetail)
                .toList();

        result.setRecords(records);
        return result;
    }

    @Override
    @Transactional
    public void markProductSold(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getIsDeleted() == 1) {
//            throw new BusinessException("商品不存在");
        }

        Integer status = product.getProductStatus();

        if (status == 4) {
            return; // 已售，直接忽略
        }

        if (status != 2) {
//            throw new BusinessException("当前状态不允许标记为已售");
        }

        productMapper.markAsSold(productId);
    }



    private ProductDetailDto toSimpleDetail(Product product) {

        ProductDetailDto detail = new ProductDetailDto();
        detail.setId(product.getId());
        detail.setTitle(product.getTitle());
        detail.setPrice(product.getPrice());
        detail.setQuantity(product.getQuantity());
        detail.setConditionLevel(product.getConditionLevel());
        detail.setNegotiable(product.getNegotiable());
        detail.setViewCount(product.getViewCount());

//        // 只取封面图（第一张）
//        ProductImageDto images=
//                productImageMapper.selectFirstByProductId(product.getId());
//
//        detail.setImages(image);
        return detail;
    }


}




