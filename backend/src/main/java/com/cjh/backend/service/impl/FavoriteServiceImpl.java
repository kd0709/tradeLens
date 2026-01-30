package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.Favorite.FavoriteListDto;
import com.cjh.backend.dto.Favorite.FavoriteToggleDto;
import com.cjh.backend.entity.Favorite;
import com.cjh.backend.mapper.ProductMapper;
import com.cjh.backend.service.FavoriteService;
import com.cjh.backend.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author 45209
* @description 针对表【favorite(商品收藏表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
    implements FavoriteService{


    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long userId, FavoriteToggleDto dto) {
        Long existId = favoriteMapper.selectIdByUserAndProduct(userId, dto.getProductId());

        if (existId != null) {
            // 已收藏 → 取消
            int rows = favoriteMapper.deleteById(existId);
            if (rows == 0) {
                throw new IllegalStateException("取消收藏失败");
            }
            return false; // false 表示已取消
        }
        // 未收藏 → 添加
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(dto.getProductId());
        favorite.setCreateTime(LocalDateTime.now());

        int rows = favoriteMapper.insert(favorite);
        if (rows == 0) {
            throw new IllegalStateException("添加收藏失败");
        }
        return true; // true 表示已收藏
    }

    @Override
    public List<FavoriteListDto> listFavorites(Long userId) {
        return favoriteMapper.selectFavoriteListByUserId(userId);
    }
}




