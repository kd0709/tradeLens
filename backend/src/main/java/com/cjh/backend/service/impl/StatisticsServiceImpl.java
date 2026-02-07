package com.cjh.backend.service.impl;

import com.cjh.backend.dto.data.SalesStatisticsDto;
import com.cjh.backend.mapper.StatisticsMapper;
import com.cjh.backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public SalesStatisticsDto getSellerStatistics(Long sellerId) {

        SalesStatisticsDto dto = new SalesStatisticsDto();

        // 1. 获取基础汇总信息
        SalesStatisticsDto.SellerSummaryDto sellerSummaryDto = statisticsMapper.selectSellerSummary(sellerId);

        // 防止空指针
        if (sellerSummaryDto == null) {
            sellerSummaryDto = new SalesStatisticsDto.SellerSummaryDto();
            sellerSummaryDto.setTotalAmount(BigDecimal.ZERO);
            sellerSummaryDto.setTotalOrders(0L);
        }

        dto.setSellerData(sellerSummaryDto);

        BigDecimal totalAmount = sellerSummaryDto.getTotalAmount();
        Long totalOrders = sellerSummaryDto.getTotalOrders();

        // 2. 计算客单价
        if (totalOrders == null || totalOrders == 0) {
            dto.setAvgPrice(BigDecimal.ZERO);
        } else {
            dto.setAvgPrice(
                    totalAmount.divide(
                            BigDecimal.valueOf(totalOrders),
                            2,
                            RoundingMode.HALF_UP
                    )
            );
        }


        // 处理最近 7 天销售趋势

        List<SalesStatisticsDto.DailySalesDto> rawTrend = statisticsMapper.selectSellerLast7DaysTrend(sellerId);


        Map<String, BigDecimal> salesMap = rawTrend.stream()
                .collect(Collectors.toMap(
                        SalesStatisticsDto.DailySalesDto::getDate,
                        SalesStatisticsDto.DailySalesDto::getAmount,
                        (v1, v2) -> v1 // 如果有重复日期(通常SQL group by保证了不会有)，取第一个
                ));

        // 3.3 构造完整的 7 天数据列表
        List<SalesStatisticsDto.DailySalesDto> fullTrend = new ArrayList<>();

        // 格式化器，必须和 SQL 中的 DATE_FORMAT(create_time, '%m-%d') 保持一致
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();

        // 循环过去 6 天 + 今天 = 7 天
        for (int i = 6; i >= 0; i--) {
            // 计算日期：今天减去 i 天
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);

            // 创建 DTO 对象
            SalesStatisticsDto.DailySalesDto dailyDto = new SalesStatisticsDto.DailySalesDto();
            dailyDto.setDate(dateStr);

            // 核心补零逻辑：去 Map 里找，找不到就给 ZERO
            BigDecimal amount = salesMap.getOrDefault(dateStr, BigDecimal.ZERO);
            dailyDto.setAmount(amount);

            fullTrend.add(dailyDto);
        }

        // 3.4 设置补全后的完整列表
        dto.setLast7DaysTrend(fullTrend);


        // 4. 分类占比
        dto.setCategoryStats(
                statisticsMapper.selectSellerCategoryStats(sellerId)
        );

        return dto;
    }
}
