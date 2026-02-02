// 订单状态枚举
export enum OrderStatus {
  UNPAID = 1,   // 待支付
  PAID = 2,     // 待发货
  SHIPPED = 3,  // 已发货
  COMPLETED = 4,// 已完成
  CANCELLED = 5 // 已取消
}


// 订单明细项
export interface OrderItemDto {
  id: number
  productId: number
  productTitle: string
  price: number
  quantity: number
}

// 订单数据传输对象
export interface OrderDto {
  id: number
  orderNo: string
  buyerId: number
  sellerId: number
  totalPrice: number
  status: number  // 订单状态：1待支付 2待发货 3已发货 4已完成 5已取消
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  trackingNo?: string
  createTime: string
  payTime?: string
  deliveryTime?: string
  finishTime?: string
  items?: OrderItemDto[]  // 订单明细（详情接口返回）
}

// 订单列表查询参数
export interface OrderQuery {
  page: number
  size: number
  status?: number
}