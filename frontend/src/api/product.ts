import request from './request'
import type { ProductDto, ProductQuery, PageResult } from '@/dto/product'


// 发布商品 DTO
export interface ProductPublishDto {
  title: string
  description: string
  price: number
  categoryId: number
  conditionLevel: number
  images: string[]
}

// 获取商品列表 (对应 /api/product/list)
export function getProductList(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/list', { params: query })
}

// 获取商品分类 (对应 /api/category/list - 假设有此接口，为了首页分类导航)
export function getCategories(): Promise<any[]> {
  return request.get('/api/category/list')
}

// 获取商品详情 (对应 /api/product/detail/{id})
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