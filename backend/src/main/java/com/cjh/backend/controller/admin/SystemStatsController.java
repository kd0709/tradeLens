package com.cjh.backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cjh.backend.entity.Category;
import com.cjh.backend.entity.Orders;
import com.cjh.backend.entity.Product;
import com.cjh.backend.entity.User;
import com.cjh.backend.service.CategoryService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final CategoryService categoryService;

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

        // --- 新增：5. 近7天交易额趋势 ---
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); // 包含今天，共7天

        // 获取近7天的所有有效订单
        List<Orders> recentOrders = ordersService.list(new LambdaQueryWrapper<Orders>()
                .in(Orders::getStatus, 3, 4)
                .ge(Orders::getCreateTime, sevenDaysAgo.atStartOfDay()));

        // 初始化连续7天的日期和数据 (格式: MM-dd)
        List<String> dateList = new ArrayList<>();
        List<BigDecimal> salesList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 0; i < 7; i++) {
            LocalDate date = sevenDaysAgo.plusDays(i);
            String dateStr = date.format(formatter);
            dateList.add(dateStr);

            // 计算该天的销售额
            BigDecimal dailySales = recentOrders.stream()
                    .filter(order -> order.getCreateTime().toLocalDate().equals(date))
                    .map(Orders::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            salesList.add(dailySales);
        }

        Map<String, Object> trendData = new HashMap<>();
        trendData.put("dates", dateList);
        trendData.put("sales", salesList);
        stats.put("trendData", trendData);


        // --- 新增：6. 商品分类占比 (取在售商品) ---
        List<Product> allActiveProducts = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getIsDeleted, 0)
                .eq(Product::getProductStatus, 2));

        // 按 categoryId 分组统计商品数量
        Map<Long, Long> categoryCountMap = allActiveProducts.stream()
                .filter(p -> p.getCategoryId() != null)
                .collect(Collectors.groupingBy(Product::getCategoryId, Collectors.counting()));

        // 获取所有涉及的分类名称
        List<Long> categoryIds = new ArrayList<>(categoryCountMap.keySet());
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryService.listByIds(categoryIds);
            categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
        }

        // 组装饼图所需的数据结构 [{value: 数量, name: '分类名'}]
        List<Map<String, Object>> categoryDataList = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : categoryCountMap.entrySet()) {
            String catName = categoryNameMap.getOrDefault(entry.getKey(), "未知分类");
            Map<String, Object> item = new HashMap<>();
            item.put("name", catName);
            item.put("value", entry.getValue());
            categoryDataList.add(item);
        }

        // 如果没有商品，给个默认空数据
        if(categoryDataList.isEmpty()){
            Map<String, Object> emptyItem = new HashMap<>();
            emptyItem.put("name", "暂无数据");
            emptyItem.put("value", 0);
            categoryDataList.add(emptyItem);
        }

        stats.put("categoryData", categoryDataList);

        return Result.success(stats);
    }
}