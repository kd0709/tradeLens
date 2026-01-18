package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.OrderCreateDTO;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.entity.Product;
import com.cjh.backend.mapper.ProductMapper;
import com.cjh.backend.service.OrdersService;
import com.cjh.backend.mapper.OrdersMapper;
import com.cjh.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author 45209
* @description 针对表【orders(订单表)】的数据库操作Service实现
* @createDate 2026-01-18 22:32:09
*/
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{

    private final OrdersMapper ordersMapper;
    private final ProductMapper productMapper;
    private final ProductService productService;

    @Transactional
    @Override
    public Long createOrder(Long buyerId, OrderCreateDTO dto) {

        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getIsDeleted() == 1) {
//            throw new BusinessException("商品不存在");
        }

        if (!product.getProductStatus().equals(2)) {
//            throw new BusinessException("商品不可购买");
        }

        if (product.getUserId().equals(buyerId)) {
//            throw new BusinessException("不能购买自己发布的商品");
        }

        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setProductId(product.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getUserId());
        order.setTotalPrice(product.getPrice());
        order.setOrderStatus(1);

        ordersMapper.insert(order);

        return order.getId();
    }


    @Transactional
    @Override
    public void payOrder(Long orderId, Long buyerId) {

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
//            throw new BusinessException("订单不存在");
        }

        if (!order.getBuyerId().equals(buyerId)) {
//            throw new BusinessException("无权支付该订单");
        }

        if (!order.getOrderStatus().equals(1)) {
//            throw new BusinessException("订单状态异常");
        }

        order.setOrderStatus(2);
        ordersMapper.updateById(order);
    }


    @Transactional
    @Override
    public void completeOrder(Long orderId, Long sellerId) {

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
//            throw new BusinessException("订单不存在");
        }

        if (!order.getSellerId().equals(sellerId)) {
//            throw new BusinessException("无权操作该订单");
        }

        if (!order.getOrderStatus().equals(2)) {
//            throw new BusinessException("订单未支付");
        }

        // 更新订单状态
        order.setOrderStatus(3);
        ordersMapper.updateById(order);

        // 商品置为已售
        productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, order.getProductId())
                        .eq(Product::getProductStatus, 2)
                        .set(Product::getProductStatus, 4)
        );
    }
    @Transactional
    @Override
    public void cancelOrder(Long orderId, Long buyerId) {

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
//            throw new BusinessException("订单不存在");
        }

        if (!order.getBuyerId().equals(buyerId)) {
//            throw new BusinessException("无权取消");
        }

        if (!order.getOrderStatus().equals(1)) {
//            throw new BusinessException("当前状态不能取消");
        }

        order.setOrderStatus(4);
        ordersMapper.updateById(order);
    }


    private String generateOrderNo() {
        return "OD" + System.currentTimeMillis();
    }

}




