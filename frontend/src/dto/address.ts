export interface AddressDto {
  id?: number
  userId?: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: 0 | 1
}