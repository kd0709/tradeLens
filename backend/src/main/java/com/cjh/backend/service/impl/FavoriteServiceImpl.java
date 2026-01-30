package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.entity.Favorite;
import com.cjh.backend.service.FavoriteService;
import com.cjh.backend.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author 45209
* @description 针对表【favorite(商品收藏表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
    implements FavoriteService{

}




