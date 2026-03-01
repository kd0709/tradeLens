import request from './request'
import type { SalesStatisticsDto } from '@/dto/statistics'

export function getSalesStatistics(): Promise<SalesStatisticsDto> {
  return request.get('/api/statistics/sales')
}
