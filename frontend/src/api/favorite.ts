import request from './request'
import type { FavoriteToggleDto } from '@/dto/favorite'

export function toggleFavorite(data: FavoriteToggleDto): Promise<boolean> {
  return request.post('/api/favorite/toggle', data)
}

export function getFavoriteList(): Promise<any[]> {
  return request.get('/api/favorite/list')
}
