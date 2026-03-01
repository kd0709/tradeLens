import request from './request'
import type { CommentPublishDto } from '../dto/comment'

export function publishComment(data: CommentPublishDto): Promise<void> {
  return request.post('/api/comment/publish', data)
}

export function getProductComments(productId: number, page = 1, size = 10): Promise<any> {
  return request.get(`/api/comment/list/${productId}`, { params: { page, size } })
}
