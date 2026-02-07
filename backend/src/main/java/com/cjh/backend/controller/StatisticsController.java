package com.cjh.backend.controller;


import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.dto.data.SalesStatisticsDto;
import com.cjh.backend.service.StatisticsService;
import com.cjh.backend.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/sales")
    public Result<SalesStatisticsDto> getSalesStatistics(@CurrentUser Long userId) {
        log.info("获取用户 {} 统计数据", userId );
        try {
            SalesStatisticsDto stats = statisticsService.getSellerStatistics(userId);
            log.info("用户获取统计数据成功：userId={}", userId);
            return Result.success(stats, "获取成功");
        } catch (IllegalArgumentException e) {
            log.warn("用户 {} 获取统计数据失败：{}", userId, e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("用户 {} 获取统计数据失败", userId, e);
            return Result.fail("获取失败，请稍后重试");
        }

    }
}