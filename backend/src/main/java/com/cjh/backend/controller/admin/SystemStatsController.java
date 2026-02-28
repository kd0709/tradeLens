package com.cjh.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.entity.Product;
import com.cjh.backend.entity.User;
import com.cjh.backend.service.OrdersService;
import com.cjh.backend.service.ProductService;
import com.cjh.backend.service.UserService;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理系统 - 大盘数据统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/stats")
@RequiredArgsConstructor
public class SystemStatsController {

    private final UserService userService;
    private final ProductService productService;
    private final OrdersService ordersService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        log.info("System Admin - 获取大盘统计数据");
        Map<String, Object> stats = new HashMap<>();

        // 1. 全站总用户数 (排除已注销/删除的)
        long totalUsers = userService.count(new LambdaQueryWrapper<User>().eq(User::getIsDeleted, 0));
        stats.put("totalUsers", totalUsers);

        // 2. 当前在售商品数 (状态2为上架)
        long activeProducts = productService.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getIsDeleted, 0)
                .eq(Product::getProductStatus, 2));
        stats.put("activeProducts", activeProducts);

        // 3. 历史总订单数
        long totalOrders = ordersService.count();
        stats.put("totalOrders", totalOrders);

        // 4. 累计交易总额 (状态为 3已发货 或 4已完成 的视为有效交易)
        List<Orders> validOrders = ordersService.list(new LambdaQueryWrapper<Orders>()
                .in(Orders::getStatus, 3, 4));
        BigDecimal totalSales = validOrders.stream()
                .map(Orders::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalSales", totalSales != null ? totalSales : BigDecimal.ZERO);

        return Result.success(stats);
    }
}