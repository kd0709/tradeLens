import request from './request'
import type { ProductDto, ProductQuery, ProductPublishDto } from '@/dto/product'
import type { PageResult } from '@/dto/common'

// 发布商品接口
export function publishProduct(data: ProductPublishDto): Promise<number> {
  return request.post('/api/product/publish', data)
}

// 修改商品状态
export function updateProductStatus(data: ProductPublishDto & { status: number }): Promise<number> {
  return request.put('/api/product/status', data)
}

// 删除商品接口
export function deleteProduct(productId: number): Promise<void> {
  return request.delete(`/api/product/${productId}`)
}

// 获取我的商品
export function getMyProducts(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/my', { params: query }) // 修正为 /my
}

// 获取商品列表 
export function getProductList(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/list', { params: query })
}

// 获取商品详情
export function getProductDetail(id: number): Promise<ProductDto> {
  return request.get(`/api/product/${id}`) // 去掉 /detail
}

// 编辑商品接口
export function updateProduct(data: ProductPublishDto & { id: number }): Promise<void> {
  return request.put('/api/product', data)
}


