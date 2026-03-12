import request from './request'
import type { ProductStatusUpdateDto, ProductDto, ProductQuery, ProductPublishDto, ProductUpdateDto } from '@/dto/product'
import type { PageResult } from '@/dto/common'

export function publishProduct(data: ProductPublishDto): Promise<number> {
  return request.post('/api/product/publish', data)
}

export function updateProductStatus(data: ProductStatusUpdateDto): Promise<number> {
  return request.put('/api/product/status', data)
}

export function deleteProduct(productId: number): Promise<void> {
  return request.delete(`/api/product/${productId}`)
}

export function getMyProducts(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/my', {
    params: {
      current: query.current,
      size: query.size,
      status: query.status
    }
  })
}

export function getProductList(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/list', {
    params: {
      current: query.current,
      size: query.size,
      keyword: query.keyword,
      categoryId: query.categoryId,
      condition: query.condition,
      sort: query.sort
    }
  })
}

export function getProductDetail(id: number): Promise<ProductDto> {
  return request.get(`/api/product/${id}`)
}

export function updateProduct(data: ProductUpdateDto): Promise<void> {
  return request.put('/api/product', data)
}

export const getRecommendProducts = (limit: number = 10) => {
  return request.get('/api/product/recommend', { params: { limit } })
}