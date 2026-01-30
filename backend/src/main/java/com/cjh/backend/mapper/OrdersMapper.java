package com.cjh.backend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjh.backend.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author 45209
* @description 针对表【orders(订单表)】的数据库操作Mapper
* @createDate 2026-01-29 18:58:49
* @Entity com.cjh.backend.entity.Orders
*/

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 买家订单分页列表
     */
    @Select("""
        SELECT * FROM tradelens.orders 
        WHERE buyer_id = #{buyerId} 
          AND (#{status} IS NULL OR status = #{status})
        ORDER BY create_time DESC
        """)
    IPage<Orders> selectBuyerOrders(Page<Orders> page,
                                    @Param("buyerId") Long buyerId,
                                    @Param("status") Integer status);

    /**
     * 卖家订单分页列表
     */
    @Select("""
        SELECT * FROM tradelens.orders 
        WHERE seller_id = #{sellerId} 
          AND (#{status} IS NULL OR status = #{status})
        ORDER BY create_time DESC
        """)
    IPage<Orders> selectSellerOrders(Page<Orders> page,
                                     @Param("sellerId") Long sellerId,
                                     @Param("status") Integer status);

    /**
     * 根据订单号查询
     */
    @Select("SELECT * FROM tradelens.orders WHERE order_no = #{orderNo}")
    Orders selectByOrderNo(@Param("orderNo") String orderNo);


}




