import request from './request'
import type { CartListDto , AddToCartDto , UpdateCartQuantityDto } from '@/dto/cart'

// 加入购物车
export function addToCart(data: AddToCartDto): Promise<void> {
  return request.post('/api/cart/add', data)
}

// 获取购物车列表
export function getCartList(): Promise<CartListDto[]> {
  return request.get('/api/cart/list')
}

// 更新数量
export function updateCartQuantity(data: UpdateCartQuantityDto): Promise<void> {
  return request.put('/api/cart/update', data)
}

// 删除购物车商品 (支持批量)
export function deleteCartItems(cartIds: number[]): Promise<void> {
  return request.delete('/api/cart/delete', {
    params: { cartIds }   
  })
}
