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

        SalesStatisticsDto.SellerSummaryDto sellerSummaryDto = statisticsMapper.selectSellerSummary(sellerId);
        if (sellerSummaryDto == null) {
            sellerSummaryDto = new SalesStatisticsDto.SellerSummaryDto();
            sellerSummaryDto.setTotalAmount(BigDecimal.ZERO);
            sellerSummaryDto.setTotalOrders(0L);
        }
        dto.setSellerData(sellerSummaryDto);

        BigDecimal totalAmount = sellerSummaryDto.getTotalAmount();
        Long totalOrders = sellerSummaryDto.getTotalOrders();

        if (totalOrders == null || totalOrders == 0) {
            dto.setAvgPrice(BigDecimal.ZERO);
        } else {
            dto.setAvgPrice(totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP));
        }

        List<SalesStatisticsDto.DailySalesDto> rawTrend = statisticsMapper.selectSellerLast7DaysTrend(sellerId);
        Map<String, BigDecimal> salesMap = rawTrend.stream()
                .collect(Collectors.toMap(
                        SalesStatisticsDto.DailySalesDto::getDate,
                        SalesStatisticsDto.DailySalesDto::getAmount,
                        (v1, v2) -> v1
                ));

        List<SalesStatisticsDto.DailySalesDto> fullTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);

            SalesStatisticsDto.DailySalesDto dailyDto = new SalesStatisticsDto.DailySalesDto();
            dailyDto.setDate(dateStr);
            dailyDto.setAmount(salesMap.getOrDefault(dateStr, BigDecimal.ZERO));
            fullTrend.add(dailyDto);
        }
        dto.setLast7DaysTrend(fullTrend);

        dto.setCategoryStats(statisticsMapper.selectSellerCategoryStats(sellerId));

        return dto;
    }
}
