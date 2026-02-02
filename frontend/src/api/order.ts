import request from './request'
import type { OrderDto, OrderQuery } from '@/dto/order'
import type { PageResult } from '@/dto/product'



// 订单列表接口（后端返回PageDto格式）
export function getBuyerOrders(query: OrderQuery): Promise<PageResult<OrderDto>> {
  return request.get('/api/order/buyer/list', { 
    params: { 
      current: query.page,  // 后端使用current作为页码参数
      size: query.size,
      status: query.status 
    } 
  })
}

export function getSellerOrders(query: OrderQuery): Promise<PageResult<OrderDto>> {
  return request.get('/api/order/seller/list', { 
    params: { 
      current: query.page,  // 后端使用current作为页码参数
      size: query.size,
      status: query.status 
    } 
  })
}

// 修改详情接口，注意参数改为 orderNo
export function getOrderDetail(orderNo: string): Promise<OrderDto> {
  return request.get(`/api/order/detail/${orderNo}`) // 修正路径和参数
}

// 修改发货接口，适配后端 PathVariable + RequestParam
export function deliverOrder(data: { orderNo: string; trackingNo: string }): Promise<void> {
  return request.post(`/api/order/deliver/${data.orderNo}?trackingNo=${data.trackingNo}`)
}

// 修改确认收货
export function confirmOrder(orderNo: string): Promise<void> {
  return request.post(`/api/order/receive/${orderNo}`) // 修正路径 confirm -> receive
}

// 修改取消订单
export function cancelOrder(orderNo: string): Promise<void> {
  return request.post(`/api/order/cancel/${orderNo}`)
}


// 创建订单 (保持不变)
export function createOrder(data: { productId: number; quantity: number; addressId: number }): Promise<any> {
  return request.post('/api/order/create', data)
}

// 支付订单
export function payOrder(data: { orderNo: string; payType: string }): Promise<void> {
  return request.post('/api/order/pay', data)
}


