package com.cjh.backend.mapper;

import com.cjh.backend.dto.Product.ProductDetailDto;
import com.cjh.backend.dto.Product.ProductListDto;
import com.cjh.backend.dto.Product.ProductMyDto;
import com.cjh.backend.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjh.backend.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
* @author 45209
* @description 针对表【product(商品表)】的数据库操作Mapper
* @createDate 2026-01-29 18:58:49
* @Entity com.cjh.backend.entity.Product
*/

@Mapper
public interface ProductMapper extends BaseMapper<Product> {


    int insertProduct(Product product);

    int insertProductImage(ProductImage image);

    /**
     * 更新商品状态（仅限自己的商品，且只能从允许的状态变更）
     */
    int updateProductStatus(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("status") Integer status
    );

    /**
     * 逻辑删除商品（设置 is_deleted = 1）
     * 同时删除关联的图片记录（可选，根据业务决定是否保留图片记录）
     */
    int deleteProductByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    /**
     * 查询我的发布商品列表（分页 + 状态过滤）
     */
    List<ProductMyDto> listMyProducts(
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    /**
     * 统计我的发布商品总数（状态过滤）
     */
    int countMyProducts(
            @Param("userId") Long userId,
            @Param("status") Integer status
    );

    List<ProductListDto> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("condition") Integer condition,
            @Param("sort") String sort,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    int countSearchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("condition") Integer condition
    );

    /**
     * 获取商品详情（主信息 + 卖家基本信息 + 图片列表）
     */
    @Select("""
    SELECT 
        p.id,
        p.title,
        p.description,
        p.price,
        p.quantity,
        p.condition_level AS conditionLevel,
        p.negotiable,
        p.product_status AS productStatus,
        p.view_count AS viewCount,
        p.create_time AS createTime,
        p.update_time AS updateTime,
        
        p.user_id AS sellerId,
        u.nickname AS sellerNickname,
        u.avatar AS sellerAvatar
    FROM tradelens.product p
    INNER JOIN tradelens.user u ON p.user_id = u.id
    WHERE p.id = #{productId}
      AND p.is_deleted = 0
      AND p.product_status = 2   -- 只返回已上架的（或根据业务调整）
""")
    ProductDetailDto getProductDetailById(@Param("productId") Long productId);

    /**
     * 检查当前用户是否收藏该商品
     */
    @Select("""
    SELECT COUNT(*) > 0
    FROM tradelens.favorite
    WHERE user_id = #{userId}
      AND product_id = #{productId}
""")
    boolean isFavorited(
            @Param("userId") Long userId,
            @Param("productId") Long productId
    );

    /**
     * 增加商品浏览量 +1
     */
    @Update("UPDATE tradelens.product SET view_count = view_count + 1 WHERE id = #{productId}")
    int incrementViewCount(@Param("productId") Long productId);

    @Select("SELECT image_url FROM tradelens.product_image WHERE product_id = #{productId} ORDER BY sort ASC")
    List<String> getProductImageUrls(@Param("productId") Long productId);
}




