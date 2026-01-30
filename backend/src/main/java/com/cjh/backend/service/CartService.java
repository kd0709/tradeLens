package com.cjh.backend.service;

import com.cjh.backend.dto.Cart.AddToCartDto;
import com.cjh.backend.dto.Cart.CartListDto;
import com.cjh.backend.dto.Cart.UpdateCartQuantityDto;
import com.cjh.backend.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 45209
* @description 针对表【cart(购物车表)】的数据库操作Service
* @createDate 2026-01-29 18:58:49
*/
public interface CartService extends IService<Cart> {

    /**
     * 加入购物车
     */
    Long addToCart(Long userId, AddToCartDto dto);

    /**
     * 购物车列表
     */
    List<CartListDto> listCart(Long userId);

    /**
     * 更新购物车数量
     */
    void updateQuantity(Long userId, UpdateCartQuantityDto dto);

    /**
     * 删除购物车项（支持批量）
     */
    void deleteCartItems(Long userId, List<Long> cartIds);
}
