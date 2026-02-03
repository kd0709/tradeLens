import request from './request'
import type { ProductStatusUpdateDto, ProductDto, ProductQuery, ProductPublishDto, ProductUpdateDto } from '@/dto/product'
import type { PageResult } from '@/dto/common'

// 发布商品接口
export function publishProduct(data: ProductPublishDto): Promise<number> {
  return request.post('/api/product/publish', data)
}

// 修改商品状态
export function updateProductStatus(data: ProductStatusUpdateDto): Promise<number> {
  return request.put('/api/product/status', data)
}

// 删除商品接口
export function deleteProduct(productId: number): Promise<void> {
  return request.delete(`/api/product/${productId}`)
}

// 获取我的商品
export function getMyProducts(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/my', { 
    params: {
      current: query.current,
      size: query.size,
      status: query.status
    } 
  })       
}

// 商品搜索列表 
export function getProductList(query: ProductQuery): Promise<PageResult<ProductDto>> {
  return request.get('/api/product/list', { 
    params:{
      current: query.current,
      size: query.size,
      keyword: query.keyword,
      categoryId: query.categoryId,
      condition: query.condition,
      sort: query.sort  
    }  
  })
}

// 获取商品详情
export function getProductDetail(id: number): Promise<ProductDto> {
  return request.get(`/api/product/${id}`) 
}

// 编辑商品接口
export function updateProduct(data: ProductUpdateDto): Promise<void> {
  return request.put('/api/product', data)
}


