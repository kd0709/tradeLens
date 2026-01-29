import request from './request'
import type { OrderDto, OrderQuery } from '@/dto/order'
import type { PageResult } from '@/dto/product'

// --- 订单操作 ---

// 创建订单
export function createOrder(data: { productId: number; quantity: number; addressId: number }): Promise<number> {
  return request.post('/api/order/create', data)
}

// 支付订单
export function payOrder(orderId: number): Promise<void> {
  return request.post(`/api/order/pay/${orderId}`)
}

// 卖家发货
export function deliverOrder(data: { orderNo: string; trackingNo: string }): Promise<void> {
  return request.post('/api/order/deliver', data)
}

// 确认收货
export function confirmOrder(orderId: number): Promise<void> {
  return request.post(`/api/order/confirm/${orderId}`)
}

// 取消订单
export function cancelOrder(orderId: number): Promise<void> {
  return request.post(`/api/order/cancel/${orderId}`)
}

// --- 列表查询 ---

// 买家订单列表
export function getBuyerOrders(query: OrderQuery): Promise<PageResult<OrderDto>> {
  return request.get('/api/order/buyer', { params: query })
}

// 卖家订单列表
export function getSellerOrders(query: OrderQuery): Promise<PageResult<OrderDto>> {
  return request.get('/api/order/seller', { params: query })
}

// 获取订单详情
export function getOrderDetail(orderId: number): Promise<OrderDto> {
  return request.get(`/api/order/${orderId}`)
}