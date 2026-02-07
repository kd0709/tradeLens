package com.cjh.backend.mapper;

import com.cjh.backend.dto.data.SalesStatisticsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StatisticsMapper {

    SalesStatisticsDto.SellerSummaryDto selectSellerSummary(
            @Param("sellerId") Long sellerId
    );


    List<SalesStatisticsDto.DailySalesDto> selectSellerLast7DaysTrend(
            @Param("sellerId") Long sellerId
    );

    List<SalesStatisticsDto.CategoryStatDto> selectSellerCategoryStats(
            @Param("sellerId") Long sellerId
    );
}
