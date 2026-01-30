import request from './request'
import type { AddressDto } from '@/dto/address'


// 获取地址列表
export function getAddressList(): Promise<AddressDto[]> {
  return request.get('/api/address/list')
}

// 添加新地址
export function addAddress(data: AddressDto): Promise<void> {
  return request.post('/api/address', data)
}

// 更新地址
export function updateAddress(data: AddressDto): Promise<void> {
  return request.put('/api/address', data)
}

// 删除地址
export function deleteAddress(id: number): Promise<void> {
  return request.delete(`/api/address/${id}`)
}