import { addToCart } from "@/api/cart"

export interface CartListDto {
  id: number
  productId: number
  productTitle: string
  price: number
  quantity: number
  productImage: string
  createTime: string
}

export interface AddToCartDto {
  productId: number
  quantity: number
}

export interface UpdateCartQuantityDto {
  id: number
  quantity: number
}