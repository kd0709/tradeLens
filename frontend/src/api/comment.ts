import request from './request'

// 发布评价
export interface CommentDto {
  orderId: number
  productId: number
  score: number
  content: string
}

export function publishComment(data: CommentDto): Promise<void> {
  return request.post('/api/comment/publish', data)
}

// 获取商品评价列表
export function getProductComments(productId: number, page = 1, size = 10): Promise<any> {
  return request.get(`/api/comment/list/${productId}`, { params: { page, size } })
}