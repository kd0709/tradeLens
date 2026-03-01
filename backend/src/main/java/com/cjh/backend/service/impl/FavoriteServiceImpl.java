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
            int rows = favoriteMapper.deleteById(existId);
            if (rows == 0) {
                throw new IllegalStateException("取消收藏失败");
            }
            return false;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(dto.getProductId());
        favorite.setCreateTime(LocalDateTime.now());

        int rows = favoriteMapper.insert(favorite);
        if (rows == 0) {
            throw new IllegalStateException("添加收藏失败");
        }
        return true;
    }

    @Override
    public List<FavoriteListDto> listFavorites(Long userId) {
        return favoriteMapper.selectFavoriteListByUserId(userId);
    }
}