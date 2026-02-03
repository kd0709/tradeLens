import request from './request'
import type { CommentPublishDto } from '../dto/comment'

// 发布商品评价
export function publishComment(data: CommentPublishDto): Promise<void> {
  return request.post('/api/comment/publish', data)
}

// 获取商品评价列表
export function getProductComments(productId: number, page = 1, size = 10): Promise<any> {
  return request.get(`/api/comment/list/${productId}`, { params: { page, size } })
}