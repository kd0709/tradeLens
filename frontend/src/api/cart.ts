import request from './request'
import type { CartItemDto } from '@/dto/cart'

// 获取购物车列表
export function getCartList(): Promise<CartItemDto[]> {
  return request.get('/api/cart/list')
}

// 加入购物车
export function addToCart(data: { productId: number; quantity: number }): Promise<void> {
  return request.post('/api/cart/add', data)
}

// 更新数量
export function updateCartItem(id: number, quantity: number): Promise<void> {
  return request.put('/api/cart/update', { id, quantity })
}

// 删除购物车商品 (支持批量)
export function deleteCartItems(ids: number[]): Promise<void> {
  return request.delete('/api/cart/delete', { data: ids }) // Axios DELETE 传 body 需要包在 data 里
}