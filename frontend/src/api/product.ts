import request from './request'
import type { ProductDto, ProductQuery, PageResult, ProductPublishDto } from '@/dto/product'


// 获取商品列表 
export function getProductList(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/list', { params: query })
}

// 获取商品分类 
export function getCategories(): Promise<any[]> {
  return request.get('/api/category/list')
}

// 获取商品详情 
export function getProductDetail(id: number): Promise<ProductDto> {
  return request.get(`/api/product/detail/${id}`)
}

// 发布商品接口
export function publishProduct(data: ProductPublishDto): Promise<number> {
  return request.post('/api/product/publish', data)
}

// 获取当前用户的商品列表
export function getMyProducts(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/listMy', { params: query })
}