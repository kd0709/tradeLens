import request from './request'
import type { CartListDto, AddToCartDto, UpdateCartQuantityDto } from '@/dto/cart'

export function addToCart(data: AddToCartDto): Promise<void> {
  return request.post('/api/cart/add', data)
}

export function getCartList(): Promise<CartListDto[]> {
  return request.get('/api/cart/list')
}

export function updateCartQuantity(data: UpdateCartQuantityDto): Promise<void> {
  return request.put('/api/cart/update', data)
}

export function deleteCartItems(cartIds: number[]): Promise<void> {
  return request.delete('/api/cart/delete', {
    params: { cartIds }
  })
}
