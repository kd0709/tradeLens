import request from './request'



// 获取分类树
export function getCategoryTree(): Promise<any[]> {
  return request.get('/api/category/tree')
}

// 获取分类列表（平铺）
export function getCategoryList(): Promise<any[]> {
  return request.get('/api/category/list')
}


