package com.cjh.backend.mapper;

import com.cjh.backend.dto.ProductImageDto;
import com.cjh.backend.entity.ProductImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 45209
* @description 针对表【product_image(商品图片表)】的数据库操作Mapper
* @createDate 2026-01-18 17:09:39
* @Entity com.cjh.backend.entity.ProductImage
*/
public interface ProductImageMapper extends BaseMapper<ProductImage> {

    @Select("select * from tradelens.product_image where product_id=#{productId}")
    List<ProductImageDto> selectByProductId(Long productId);


    ProductImageDto selectFirstByProductId(Long productId);


    void deleteByProductId(@Param("productId") Long productId);

}




