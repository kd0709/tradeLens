export interface AddressDto {
  id?: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: number
  createTime?: 0 | 1
}

export interface AddressCreateDto {
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: 0 | 1
}

export interface AddressUpdateDto {
  id: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: 0 | 1
}