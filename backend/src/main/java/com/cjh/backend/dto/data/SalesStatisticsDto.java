package com.cjh.backend.dto.data;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesStatisticsDto {
    /** 总销售额 */
    private SellerSummaryDto sellerData;

    /** 客单价 */
    private BigDecimal avgPrice;

    /** 最近7天销售趋势 */
    private List<DailySalesDto> last7DaysTrend;

    /** 分类占比 */
    private List<CategoryStatDto> categoryStats;

    @Data
    public static class SellerSummaryDto {
        private BigDecimal totalAmount;
        private Long totalOrders;
    }

    @Data
    public static class DailySalesDto {
        private String date;      // yyyy-MM-dd
        private BigDecimal amount;
    }


    @Data
    public static class CategoryStatDto {
        private String name;      // 分类名
        private BigDecimal value; // 销售额
    }
}