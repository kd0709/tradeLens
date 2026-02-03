import request from './request'

import  type { FavoriteToggleDto } from '@/dto/favorite'

// 切换收藏状态
export function toggleFavorite(data: FavoriteToggleDto): Promise<boolean> {
  return request.post('/api/favorite/toggle', data)
}

// 获取收藏列表
export function getFavoriteList(): Promise<any[]> {
  return request.get('/api/favorite/list')
}
