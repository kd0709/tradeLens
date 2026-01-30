package com.cjh.backend.mapper;

import com.cjh.backend.entity.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 45209
* @description 针对表【order_item(订单明细表)】的数据库操作Mapper
* @createDate 2026-01-29 18:58:49
* @Entity com.cjh.backend.entity.OrderItem
*/

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

}




