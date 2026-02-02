import request from './request'
import type { ProductDto, ProductQuery, PageResult, ProductPublishDto } from '@/dto/product'


// 获取商品列表 
export function getProductList(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/list', { params: query })
}

// 获取分类树
export function getCategoryTree(): Promise<any[]> {
  return request.get('/api/category/tree')
}

// 获取分类列表（平铺）
export function getCategories(): Promise<any[]> {
  return request.get('/api/category/list')
}

// 别名导出，兼容不同命名
export const getCategoryList = getCategories

// 修正获取商品详情
export function getProductDetail(id: number): Promise<ProductDto> {
  return request.get(`/api/product/${id}`) // 去掉 /detail
}

// 修正获取我的商品
export function getMyProducts(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/my', { params: query }) // 修正为 /my
}

// 发布商品接口
export function publishProduct(data: ProductPublishDto): Promise<number> {
  return request.post('/api/product/publish', data)
}

// 编辑商品接口
export function updateProduct(data: ProductPublishDto & { id: number }): Promise<void> {
  return request.put('/api/product', data)
}
