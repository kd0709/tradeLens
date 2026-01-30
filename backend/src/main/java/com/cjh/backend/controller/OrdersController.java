package com.cjh.backend.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.dto.Orders.CreateOrderDto;
import com.cjh.backend.dto.Orders.OrderDetailDto;
import com.cjh.backend.dto.Orders.OrderListDto;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.service.OrdersService;
import com.cjh.backend.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<OrderDetailDto> createOrder(
            @CurrentUser Long userId,
            @Valid @RequestBody CreateOrderDto dto) {
        log.info("用户 {} 创建订单，商品ID: {}, 数量: {}, 地址ID: {}",
                userId, dto.getProductId(), dto.getQuantity(), dto.getAddressId());
        try {
            OrderDetailDto result = orderService.createOrder(userId, dto);
            log.info("用户 {} 创建订单成功，订单号: {}", userId, result.getOrderNo());
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户 {} 创建订单失败", userId, e);
            return Result.fail("创建订单失败：" + e.getMessage());
        }
    }

    /**
     * 买家订单列表
     */
    @GetMapping("/buyer/list")
    public Result<IPage<OrderListDto>> listBuyerOrders(
            @CurrentUser Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        log.info("用户 {} 查询买家订单列表，状态: {}, 当前页: {}, 每页: {}", userId, status, current, size);
        Page<Orders> page = new Page<>(current, size);
        return Result.success(orderService.listBuyerOrders(page, userId, status));
    }

    /**
     * 卖家订单列表
     */
    @GetMapping("/seller/list")
    public Result<IPage<OrderListDto>> listSellerOrders(
            @CurrentUser Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        log.info("用户 {} 查询卖家订单列表，状态: {}, 当前页: {}, 每页: {}", userId, status, current, size);
        Page<Orders> page = new Page<>(current, size);
        return Result.success(orderService.listSellerOrders(page, userId, status));
    }

    /**
     * 订单详情
     */
    @GetMapping("/detail/{orderNo}")
    public Result<OrderDetailDto> getOrderDetail(
            @CurrentUser Long userId,
            @PathVariable String orderNo) {
        log.info("用户 {} 查询订单详情，订单号: {}", userId, orderNo);
        try {
            OrderDetailDto dto = orderService.getOrderDetail(userId, orderNo);
            return Result.success(dto);
        } catch (Exception e) {
            log.error("用户 {} 查询订单详情失败，订单号: {}", userId, orderNo, e);
            return Result.fail("查询订单详情失败：" + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderNo}")
    public Result<String> cancelOrder(
            @CurrentUser Long userId,
            @PathVariable String orderNo) {
        log.info("用户 {} 取消订单，订单号: {}", userId, orderNo);
        try {
            orderService.cancelOrder(userId, orderNo);
            return Result.success("订单已取消");
        } catch (Exception e) {
            log.error("用户 {} 取消订单失败，订单号: {}", userId, orderNo, e);
            return Result.fail("取消订单失败：" + e.getMessage());
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/receive/{orderNo}")
    public Result<String> confirmReceipt(
            @CurrentUser Long userId,
            @PathVariable String orderNo) {
        log.info("用户 {} 确认收货，订单号: {}", userId, orderNo);
        try {
            orderService.confirmReceipt(userId, orderNo);
            return Result.success("已确认收货");
        } catch (Exception e) {
            log.error("用户 {} 确认收货失败，订单号: {}", userId, orderNo, e);
            return Result.fail("确认收货失败：" + e.getMessage());
        }
    }

    /**
     * 卖家发货
     */
    @PostMapping("/deliver/{orderNo}")
    public Result<String> deliverOrder(
            @CurrentUser Long userId,
            @PathVariable String orderNo,
            @RequestParam String trackingNo) {
        log.info("用户 {} 发货，订单号: {}, 物流单号: {}", userId, orderNo, trackingNo);
        try {
            orderService.deliverOrder(userId, orderNo, trackingNo);
            return Result.success("发货成功");
        } catch (Exception e) {
            log.error("用户 {} 发货失败，订单号: {}", userId, orderNo, e);
            return Result.fail("发货失败：" + e.getMessage());
        }
    }
}
