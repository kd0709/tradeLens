import request from './request'

// ==================== 统计大盘 ====================
export function getSystemDashboardStats() {
  return request.get('/api/system/stats/dashboard')
}

// ==================== 用户管理 ====================
export function getSystemUserPage(params: { page: number; size: number; keyword?: string }) {
  return request.get('/api/system/user/page', { params })
}
export function updateSystemUser(data: any) {
  return request.put('/api/system/user', data)
}
export function deleteSystemUser(id: number) {
  return request.delete(`/api/system/user/${id}`)
}
export function addSystemUser(data: any) {
  return request.post('/api/system/user', data)
}

// ==================== 商品管理 ====================
export function getSystemProductPage(params: { page: number; size: number; keyword?: string; productStatus?: number }) {
  return request.get('/api/system/product/page', { params })
}
export function updateSystemProduct(data: any) {
  return request.put('/api/system/product', data)
}
export function deleteSystemProduct(id: number) {
  return request.delete(`/api/system/product/${id}`)
}

// ==================== 订单管理 ====================
export function getSystemOrdersPage(params: { page: number; size: number; orderNo?: string; status?: number }) {
  return request.get('/api/system/orders/page', { params })
}
export function updateSystemOrder(data: any) {
  return request.put('/api/system/orders', data)
}

// ==================== 评价管理 ====================
export function getSystemCommentPage(params: { page: number; size: number; keyword?: string }) {
  return request.get('/api/system/comment/page', { params })
}
export function deleteSystemComment(id: number) {
  return request.delete(`/api/system/comment/${id}`)
}

// ==================== 商品分类管理 ====================
export function getSystemCategoryPage(params: { page: number; size: number; keyword?: string }) {
  return request.get('/api/system/category/page', { params })
}
export function addSystemCategory(data: any) {
  return request.post('/api/system/category', data)
}
export function updateSystemCategory(data: any) {
  return request.put('/api/system/category', data)
}
export function deleteSystemCategory(id: number) {
  return request.delete(`/api/system/category/${id}`)
}