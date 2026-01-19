package com.cjh.backend.controller;

import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.dto.OrderCreateDTO;
import com.cjh.backend.service.OrdersService;
import com.cjh.backend.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    public Result<Long> create(@CurrentUser Long userId,
                               @RequestBody @Valid OrderCreateDTO dto) {

        return Result.success(ordersService.createOrder(userId, dto));
    }

    @PostMapping("/pay/{id}")
    public Result<Void> pay(@CurrentUser Long userId,
                            @PathVariable Long id) {
        ordersService.payOrder(id, userId);
        return Result.success("支付成功");
    }

    @PostMapping("/complete/{id}")
    public Result<Void> complete(@CurrentUser Long userId,
                                 @PathVariable Long id) {
        ordersService.completeOrder(id, userId);
        return Result.success("订单完成");
    }

}
