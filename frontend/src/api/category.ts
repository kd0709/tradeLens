import request from './request'

export function getCategoryTree(): Promise<any[]> {
  return request.get('/api/category/tree')
}

export function getCategoryList(): Promise<any[]> {
  return request.get('/api/category/list')
}
