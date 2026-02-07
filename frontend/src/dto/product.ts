// 对应接口文档中的 ProductDetailDto
export interface ProductDto {
  id: number
  userId: number
  categoryId: number
  title: string
  description: string
  price: number
  quantity: number
  conditionLevel: 1 | 2 | 3 | 4 // 1全新 2几乎全新 3轻微使用 4明显使用
  negotiable: 0 | 1 
  productStatus: 1 | 2 | 3 | 4 // 1待审 2上架 3下架 4已售
  images: string[] 
  viewCount: number
  sellerAvatar?: string 
  sellerNickname?: string

  createTime: string
  isLiked?: boolean // 前端辅助字段：是否已收藏
}

// 发布商品 DTO
export interface ProductPublishDto {
  title: string
  description: string
  price: number
  quantity: number        
  negotiable: 0 | 1       
  categoryId: number
  conditionLevel: number
  images: string[]
}

// 商品状态更新 DTO
export interface ProductStatusUpdateDto {
  id: number
  status: number
}


// 我的商品列表项 DTO
export interface ProductMyDto {
  id: number
  title: string
  price: number
  productStatus: number
  mainImage: string
  createTime: string
}


// 对应接口文档中的 ProductQueryDto
export interface ProductQuery {
  current: number
  size: number
  status?: 1 | 2 | 3 | 4
  keyword?: string
  categoryId?: number
  condition?: 1 | 2 | 3 | 4 // 1全新 2几乎全新 3轻微使用 4明显使用
  sort?: 'price_asc' | 'price_desc' | 'time_desc'
}


export interface ProductUpdateDto {
  id: number
  title: string
  description: string
  price: number
  categoryId: number
  conditionLevel: number
  quantity: number
  negotiable: 0 | 1
}