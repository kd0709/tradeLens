import request from './request'
import type { LoginRequest, RegisterRequest, UserInfo } from '@/dto/auth'


// 用户登录
export function login(data: LoginRequest): Promise<UserInfo> {
  return request.post('/api/auth/login', data)
}

// 用户注册（后端不需要confirmPassword，前端验证即可）
export function register(data: RegisterRequest): Promise<void> {
  // 只传username和password，confirmPassword在前端验证
  return request.post('/api/auth/register', {
    username: data.username,
    password: data.password
  })
}

// 用户登出
export function logout(): Promise<void> {
  return request.post('/api/auth/logout')
}


