package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.Cart.AddToCartDto;
import com.cjh.backend.dto.Cart.CartListDto;
import com.cjh.backend.dto.Cart.UpdateCartQuantityDto;
import com.cjh.backend.entity.Cart;
import com.cjh.backend.entity.Product;
import com.cjh.backend.mapper.ProductMapper;
import com.cjh.backend.service.CartService;
import com.cjh.backend.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author 45209
* @description 针对表【cart(购物车表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart>
    implements CartService{


    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addToCart(Long userId, AddToCartDto dto) {
        // 1. 校验商品是否存在且可购买
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getProductStatus() != 2) {
            throw new RuntimeException("商品不存在或不可加入购物车");
        }

        // 2. 检查是否已存在购物车项
        Long existCartId = cartMapper.selectCartIdByUserAndProduct(userId, dto.getProductId());
        if (existCartId != null) {
            // 已存在则更新数量
            Cart existCart = cartMapper.selectById(existCartId);
            existCart.setQuantity(existCart.getQuantity() + dto.getQuantity());
            int rows = cartMapper.updateById(existCart);
            if (rows == 0) {
                throw new IllegalStateException("更新购物车数量失败");
            }
            return existCartId;
        }

        // 3. 新增购物车项
        Cart cart = new Cart();
        BeanUtils.copyProperties(dto, cart);
        cart.setUserId(userId);
        cart.setCreateTime(LocalDateTime.now());

        int rows = cartMapper.insert(cart);
        if (rows == 0) {
            throw new IllegalStateException("加入购物车失败");
        }

        return cart.getId();
    }

    @Override
    public List<CartListDto> listCart(Long userId) {
        return cartMapper.selectCartListByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long userId, UpdateCartQuantityDto dto) {
        Cart cart = cartMapper.selectById(dto.getId());
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new RuntimeException("购物车项不存在或无权限");
        }

        cart.setQuantity(dto.getQuantity());
        int rows = cartMapper.updateById(cart);
        if (rows == 0) {
            throw new IllegalStateException("更新数量失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItems(Long userId, List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            return;
        }

        // 循环删除（毕设简单风格，不用批量删除）
        for (Long cartId : cartIds) {
            int rows = cartMapper.deleteById(cartId);
            if (rows == 0) {
//                log.warn("删除购物车项失败，ID: {}", cartId);
            }
        }
    }

}




