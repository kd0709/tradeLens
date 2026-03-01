import request from './request'
import type { UserInfo } from '@/dto/auth'

export function getUserInfo(): Promise<UserInfo> {
  return request.get('/api/user/info')
}

export function updateUserInfo(data: Partial<UserInfo>): Promise<void> {
  return request.put('/api/user/info', data)
}

export function updateUserPassword(oldPassword: string, newPassword: string): Promise<void> {
  return request.put('/api/user/password', { oldPassword, newPassword })
}
