export interface SellerSummaryDto {
  totalAmount: number
  totalOrders: number
}
export interface DailySalesDto {
  date: string
  amount: number
}
export interface CategoryStatDto {
  name: string
  value: number
}

export interface SalesStatisticsDto {
  sellerData: SellerSummaryDto
  avgPrice: number
  last7DaysTrend: DailySalesDto[]
  categoryStats: CategoryStatDto[]
}

