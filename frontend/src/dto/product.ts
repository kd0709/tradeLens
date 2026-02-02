import type { UserInfo } from './auth'

// 对应接口文档中的 ProductDetailDto
export interface ProductDto {
  id: number
  userId: number
  categoryId: number
  title: string
  description: string
  price: number
  originalPrice?: number // 原价(可选)
  quantity: number
  conditionLevel: 1 | 2 | 3 | 4 // 1全新 2几乎全新 3轻微使用 4明显使用
  negotiable: 0 | 1 // 是否可议价
  productStatus: 1 | 2 | 3 | 4 // 1待审 2上架 3下架 4已售
  images: string[] // 图片URL数组
  viewCount: number
  seller: UserInfo // 卖家信息
  createTime: string
  isLiked?: boolean // 前端辅助字段：是否已收藏
}

// 对应接口文档中的 ProductQueryDto
export interface ProductQuery {
  page: number
  size: number
  keyword?: string
  categoryId?: number
  sort?: 'price_asc' | 'price_desc' | 'time_desc'
}

// 分页响应结构
export interface PageResult<T> {
  total: number
  list: T[]
}

// 发布商品 DTO
export interface ProductPublishDto {
  title: string
  description: string
  price: number
  categoryId: number
  conditionLevel: number
  images: string[]
}
