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

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart>
        implements CartService{

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addToCart(Long userId, AddToCartDto dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getProductStatus() != 2) {
            throw new RuntimeException("商品不存在或不可加入购物车");
        }

        // 增加逻辑校验：不能将自己发布的商品加购
        if (product.getUserId().equals(userId)) {
            throw new RuntimeException("不能将自己发布的商品加入购物车");
        }

        // 读取实时库存进行后续判断校验
        Integer currentStock = productMapper.checkProductStock(dto.getProductId());

        Long existCartId = cartMapper.selectCartIdByUserAndProduct(userId, dto.getProductId());
        if (existCartId != null) {
            Cart existCart = cartMapper.selectById(existCartId);
            int newQuantity = existCart.getQuantity() + dto.getQuantity();

            // 增加逻辑校验：合并后数量不能超过总库存
            if (currentStock == null || newQuantity > currentStock) {
                throw new RuntimeException("购物车内总数量超出该商品当前库存限制");
            }

            existCart.setQuantity(newQuantity);
            int rows = cartMapper.updateById(existCart);
            if (rows == 0) {
                throw new IllegalStateException("更新购物车数量失败");
            }
            return existCartId;
        }

        // 增加逻辑校验：首单数量不能超过库存
        if (currentStock == null || dto.getQuantity() > currentStock) {
            throw new RuntimeException("添加数量超出该商品当前库存限制");
        }

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

        for (Long cartId : cartIds) {
            cartMapper.deleteById(cartId);
        }
    }
}