package com.cjh.backend.mapper;

import com.cjh.backend.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 45209
* @description 针对表【orders(订单表)】的数据库操作Mapper
* @createDate 2026-01-29 18:58:49
* @Entity com.cjh.backend.entity.Orders
*/

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

}




