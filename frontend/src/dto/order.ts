// 订单状态枚举
export enum OrderStatus {
  UNPAID = 1,   // 待支付
  PAID = 2,     // 待发货
  SHIPPED = 3,  // 已发货
  COMPLETED = 4,// 已完成
  CANCELLED = 5 // 已取消
}

export interface OrderDto {
  id: number
  orderNo: string
  buyerId: number
  sellerId: number
  productId: number
  productTitle?: string // 后端最好联表查询带回来，或者前端单独查
  productImage?: string // 同上
  totalPrice: number
  orderStatus: OrderStatus
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  trackingNo?: string
  createTime: string
}

// 订单列表查询参数
export interface OrderQuery {
  page: number
  size: number
  status?: number
}