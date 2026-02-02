// 分页响应结构
export interface PageResult<T> {
  total: number
  list: T[]
}