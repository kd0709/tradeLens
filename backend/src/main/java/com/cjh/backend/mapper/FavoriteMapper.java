package com.cjh.backend.mapper;

import com.cjh.backend.dto.Favorite.FavoriteListDto;
import com.cjh.backend.entity.Favorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 45209
* @description 针对表【favorite(商品收藏表)】的数据库操作Mapper
* @createDate 2026-01-29 18:58:49
* @Entity com.cjh.backend.entity.Favorite
*/


@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT id FROM tradelens.favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    Long selectIdByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    // 复杂联表查询用 XML
    List<FavoriteListDto> selectFavoriteListByUserId(@Param("userId") Long userId);
}




