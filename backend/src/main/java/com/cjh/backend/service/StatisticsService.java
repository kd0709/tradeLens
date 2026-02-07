package com.cjh.backend.service;

import com.cjh.backend.dto.data.SalesStatisticsDto;

public interface StatisticsService {

     SalesStatisticsDto getSellerStatistics(Long sellerId);
}
