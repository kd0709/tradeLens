export interface CartItemDto {
  id: number
  productId: number
  title: string
  image: string
  price: number
  quantity: number
  stock: number // 库存，用于限制最大购买数
  sellerName: string
}