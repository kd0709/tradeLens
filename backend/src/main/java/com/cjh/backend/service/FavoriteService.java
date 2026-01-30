package com.cjh.backend.service;

import com.cjh.backend.dto.Favorite.FavoriteListDto;
import com.cjh.backend.dto.Favorite.FavoriteToggleDto;
import com.cjh.backend.entity.Favorite;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 45209
* @description 针对表【favorite(商品收藏表)】的数据库操作Service
* @createDate 2026-01-29 18:58:49
*/
public interface FavoriteService extends IService<Favorite> {

    /**
     * 切换收藏（已收藏则取消，未收藏则添加）
     */
    boolean toggleFavorite(Long userId, FavoriteToggleDto dto);

    /**
     * 获取我的收藏列表
     */
    List<FavoriteListDto> listFavorites(Long userId);

}
