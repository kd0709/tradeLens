package com.cjh.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjh.backend.dto.Orders.OrdersExportDto;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.service.OrdersService;
import com.cjh.backend.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理系统 - 订单模块 CRUD 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/orders")
@RequiredArgsConstructor
public class SystemOrdersController {

    private final OrdersService ordersService;

    /**
     * 1. 分页查询全站订单 (支持按订单号精确查询、状态筛选)
     */
    @GetMapping("/page")
    public Result<Page<Orders>> getOrdersPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status) {
        
        log.info("System Admin - 分页查询订单：page={}, size={}, orderNo={}, status={}", page, size, orderNo, status);
        Page<Orders> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(orderNo)) {
            wrapper.eq(Orders::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        
        // 按创建时间倒序排
        wrapper.orderByDesc(Orders::getCreateTime);
        return Result.success(ordersService.page(pageParam, wrapper));
    }

    /**
     * 2. 查询单笔订单详情
     */
    @GetMapping("/{id}")
    public Result<Orders> getOrderById(@PathVariable("id") Long id) {
        log.info("System Admin - 查询订单详情：id={}", id);
        Orders order = ordersService.getById(id);
        if (order != null) {
            return Result.success(order);
        }
        return Result.fail(404, "订单不存在");
    }

    /**
     * 3. 修改订单信息 (通常用于管理员介入，如强制修改订单状态)
     */
    @PutMapping
    public Result<String> updateOrder(@RequestBody Orders orders) {
        if (orders.getId() == null) {
            return Result.fail(400, "订单ID不能为空");
        }
        log.info("System Admin - 修改订单信息：id={}", orders.getId());
        boolean updated = ordersService.updateById(orders);
        return updated ? Result.success("修改订单成功") : Result.fail("修改失败，订单可能不存在");
    }

    /**
     * 4. 删除订单 (危险操作，通常建议保留流水)
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable("id") Long id) {
        log.info("System Admin - 删除订单：id={}", id);
        boolean removed = ordersService.removeById(id);
        return removed ? Result.success("删除订单成功") : Result.fail("删除失败");
    }

    /**
     * 7. 导出数据
     */
    @GetMapping("/export")
    public void exportProducts(HttpServletResponse response) {
        // 假设已创建 ProductExportDto
        List<OrdersExportDto> exportList = ordersService.list().stream().map(p -> {
            OrdersExportDto dto = new OrdersExportDto();
            org.springframework.beans.BeanUtils.copyProperties(p, dto);
            return dto;
        }).collect(java.util.stream.Collectors.toList());

        com.cjh.backend.utils.ExcelUtils.export(response, "商品信息导出", "商品列表", exportList, OrdersExportDto.class);
    }
}