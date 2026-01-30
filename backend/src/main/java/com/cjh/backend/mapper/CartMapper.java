package com.cjh.backend.mapper;

import com.cjh.backend.dto.Cart.CartListDto;
import com.cjh.backend.entity.Cart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 45209
* @description 针对表【cart(购物车表)】的数据库操作Mapper
* @createDate 2026-01-29 18:58:49
* @Entity com.cjh.backend.entity.Cart
*/


@Mapper
public interface CartMapper extends BaseMapper<Cart> {
    /**
     * 查询当前用户购物车列表（关联商品信息）
     */
    List<CartListDto> selectCartListByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已将该商品加入购物车
     */
    @Select("SELECT id FROM tradelens.cart WHERE user_id = #{userId} AND product_id = #{productId}")
    Long selectCartIdByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}




