package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.OrderCreateDTO;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.entity.Product;
import com.cjh.backend.exception.BusinessException;
import com.cjh.backend.exception.ErrorConstants;
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
            throw new BusinessException(ErrorConstants.PRODUCT_NOT_EXIST);
        }

        if (!product.getProductStatus().equals(2)) {
            throw new BusinessException(ErrorConstants.PRODUCT_NOT_AVAILABLE);
        }

        if (product.getUserId().equals(buyerId)) {
            throw new BusinessException(ErrorConstants.CANNOT_BUY_OWN_PRODUCT);
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
            throw new BusinessException(ErrorConstants.ORDER_NOT_EXIST);
        }

        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ErrorConstants.ORDER_PAID_BY_OTHERS);
        }

        if (!order.getOrderStatus().equals(1)) {
            throw new BusinessException(ErrorConstants.ORDER_STATUS_INVALID);
        }

        order.setOrderStatus(2);
        ordersMapper.updateById(order);
    }


    @Transactional
    @Override
    public void completeOrder(Long orderId, Long sellerId) {

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorConstants.ORDER_NOT_EXIST);
        }

        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorConstants.ORDER_NOT_OWNED);
        }

        if (!order.getOrderStatus().equals(2)) {
            throw new BusinessException(ErrorConstants.ORDER_UNPAID);
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
            throw new BusinessException(ErrorConstants.ORDER_NOT_EXIST);
        }

        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ErrorConstants.ORDER_NOT_OWNED);
        }

        if (!order.getOrderStatus().equals(1)) {
            throw new BusinessException(ErrorConstants.ORDER_CANNOT_CANCEL);
        }

        order.setOrderStatus(4);
        ordersMapper.updateById(order);
    }


    private String generateOrderNo() {
        return "OD" + System.currentTimeMillis();
    }

}




