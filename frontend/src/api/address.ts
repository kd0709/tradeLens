import request from './request'
import type { AddressDto, AddressCreateDto, AddressUpdateDto } from '@/dto/address'

export function getMyAddressList(): Promise<AddressDto[]> {
  return request.get('/api/address/list')
}

export function createAddress(data: AddressCreateDto): Promise<void> {
  return request.post('/api/address', data)
}

export function updateAddress(data: AddressUpdateDto): Promise<void> {
  return request.put('/api/address', data)
}

export function deleteAddress(id: number): Promise<void> {
  return request.delete(`/api/address/${id}`)
}
