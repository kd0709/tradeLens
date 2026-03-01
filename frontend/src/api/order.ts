import request from './request'
import type { OrderDto, OrderQuery, CreateOrderDto, OrderPayDto, OrderDeliverDto } from '@/dto/order'
import type { PageResult } from '@/dto/common'

export function createOrder(data: CreateOrderDto): Promise<any> {
  return request.post('/api/order/create', data)
}

export function getBuyerOrders(query: OrderQuery): Promise<PageResult<OrderDto>> {
  return request.get('/api/order/buyer/list', {
    params: {
      current: query.current,
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

export function getOrderDetail(orderNo: string): Promise<OrderDto> {
  return request.get(`/api/order/detail/${orderNo}`)
}

export function payOrder(data: OrderPayDto): Promise<string> {
  return request.post('/api/order/pay', data)
}

export function deliverOrder(query: OrderDeliverDto): Promise<void> {
  return request.post(`/api/order/deliver`, null, {
    params: {
      orderNo: query.orderNo,
      trackingNo: query.trackingNo
    }
  })
}

export function confirmOrder(orderNo: string): Promise<void> {
  return request.post(`/api/order/receive/${orderNo}`)
}

export function cancelOrder(orderNo: string): Promise<void> {
  return request.post(`/api/order/cancel/${orderNo}`)
}
