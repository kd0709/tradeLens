import request from './request'
import type { AddressDto } from '@/dto/address'

export function getAddressList(): Promise<AddressDto[]> {
  return request.get('/api/address/list')
}

export function addAddress(data: AddressDto): Promise<void> {
  return request.post('/api/address', data)
}

export function updateAddress(data: AddressDto): Promise<void> {
  return request.put('/api/address', data)
}

export function deleteAddress(id: number): Promise<void> {
  return request.delete(`/api/address/${id}`)
}