import request from './request'
import type { LoginRequest, RegisterRequest, UserInfo } from '@/dto/auth'


// 用户登录
export function login(data: LoginRequest): Promise<UserInfo> {
  return request.post('/api/auth/login', data)
}

// 用户注册
export function register(data: RegisterRequest): Promise<void> {
  return request.post('/api/auth/register', data)
}

// 用户登出
export function logout(): Promise<void> {
  return request.post('/api/auth/logout')
}


