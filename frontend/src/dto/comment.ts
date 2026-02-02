export interface CommentDto {
  orderId: number  // 后端只需要orderId和score，productId不需要
  score: number
  content?: string  // 可选
}