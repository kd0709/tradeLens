import request from './request'
import type { OrderDto, OrderQuery , CreateOrderDto, OrderPayDto, OrderDeliverDto } from '@/dto/order'
import type { PageResult } from '@/dto/common'


// 创建订单 (保持不变)
export function createOrder(data: CreateOrderDto): Promise<any> {
  return request.post('/api/order/create', data)
}


// 订单列表接口（后端返回PageDto格式）
export function getBuyerOrders(query: OrderQuery): Promise<PageResult<OrderDto>> {
  return request.get('/api/order/buyer/list', { 
    params: { 
      current: query.current,  // 后端使用current作为页码参数
      size: query.size,
      status: query.status 
    } 
  })
}

export function getSellerOrders(query: OrderQuery): Promise<PageResult<OrderDto>> {
  return request.get('/api/order/seller/list', { 
    params: { 
      current: query.current,  
      size: query.size,
      status: query.status 
    } 
  })
}

// 订单详情
export function getOrderDetail(orderNo: string): Promise<OrderDto> {
  return request.get(`/api/order/detail/${orderNo}`) 
}


// 支付订单
export function payOrder(data: OrderPayDto): Promise<void> {
  return request.post('/api/order/pay', data)
}

// 订单发货
export function deliverOrder(query: OrderDeliverDto): Promise<void> {
  return request.post(`/api/order/deliver`, null ,{
    params: { 
      orderNo: query.orderNo,
      trackingNo: query.trackingNo 
    }
  })
}

// 确认收货
export function confirmOrder(orderNo: string): Promise<void> {
  return request.post(`/api/order/receive/${orderNo}`) 
}

// 取消订单
export function cancelOrder(orderNo: string): Promise<void> {
  return request.post(`/api/order/cancel/${orderNo}`)
}




