import request from './request'

// 切换收藏状态
export function toggleFavorite(productId: number): Promise<boolean> {
  return request.post('/api/favorite/toggle', { productId })
}

// 获取收藏列表
export function getFavoriteList(): Promise<any[]> {
  return request.get('/api/favorite/list')
}
