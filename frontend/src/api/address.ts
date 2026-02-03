import request from './request'
import type { AddressDto, AddressCreateDto, AddressUpdateDto } from '@/dto/address'


// 获取地址列表
export function getMyAddressList(): Promise<AddressDto[]> {
  return request.get('/api/address/list')
}

// 添加新地址
export function createAddress(data: AddressCreateDto): Promise<void> {
  return request.post('/api/address', data)
}

// 更新地址
export function updateAddress(data: AddressUpdateDto): Promise<void> {
  return request.put('/api/address', data)
}

// 删除地址
export function deleteAddress(id: number): Promise<void> {
  return request.delete(`/api/address/${id}`)
}