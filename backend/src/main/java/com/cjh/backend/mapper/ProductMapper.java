package com.cjh.backend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjh.backend.dto.ProductQueryDto;
import com.cjh.backend.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 45209
* @description 针对表【product(商品表)】的数据库操作Mapper
* @createDate 2026-01-14 14:57:56
* @Entity com.cjh.backend.entity.Product
*/
public interface ProductMapper extends BaseMapper<Product> {

    Product selectByIdAndVisible(@Param("id") Long id);

    void incrementViewCount(@Param("id") Long id);

    IPage<Product> selectPageByQuery(
            Page<Product> page,
            @Param("q") ProductQueryDto query
    );

    IPage<Product> selectMyProducts(
            Page<Product> page,
            @Param("userId") Long userId
    );

    Product selectByIdAndUser(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    int updateProductInfo(Product product);

    int updateProductStatus(
            @Param("id") Long id,
            @Param("status") Integer status
    );

    int markAsSold(@Param("id") Long id);

    int logicalDelete(
            @Param("id") Long id
    );




}




