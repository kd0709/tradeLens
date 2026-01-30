import request from './request'
import type { UserInfo } from '@/dto/auth'



// 获取用户信息
export function getUserInfo(): Promise<UserInfo> {
  return request.get('/api/user/info')
}

// 更新用户信息
export function updateUserInfo(data: Partial<UserInfo>): Promise<void> {
  return request.put('/api/user/info', data)
}

// 更新用户头像
export function updateUserAvatar(avatarUrl: string): Promise<void> {
  return request.put('/api/user/avatar', { avatarUrl })
}

// 更新用户密码
export function updateUserPassword(oldPassword: string, newPassword: string): Promise<void> {
  return request.put('/api/user/password', { oldPassword, newPassword })
}

// 获取用户收藏的商品列表