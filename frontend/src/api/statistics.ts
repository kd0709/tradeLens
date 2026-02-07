import request from './request'
import type { SalesStatisticsDto } from '@/dto/statistics'



// 用户主页获取数据
export function getSalesStatistics(): Promise<SalesStatisticsDto> {
  return request.get('/api/statistics/sales')
}