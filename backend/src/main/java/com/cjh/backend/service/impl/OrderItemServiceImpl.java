package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.entity.OrderItem;
import com.cjh.backend.service.OrderItemService;
import com.cjh.backend.mapper.OrderItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author 45209
* @description 针对表【order_item(订单明细表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem>
    implements OrderItemService{

}




