package com.cjh.backend.mapper;

import com.cjh.backend.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 45209
* @description 针对表【orders(订单表)】的数据库操作Mapper
* @createDate 2026-01-18 22:32:09
* @Entity com.cjh.backend.entity.Orders
*/
public interface OrdersMapper extends BaseMapper<Orders> {

    int updateOrderStatus(@Param("id") Long id,
                          @Param("status") Integer status);

}




