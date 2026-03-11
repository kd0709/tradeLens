import request from './request'
import type { LoginRequest, RegisterRequest, UserInfo } from '@/dto/auth'

export function login(data: LoginRequest): Promise<UserInfo> {
  return request.post('/api/auth/login', data)
}

export function register(data: RegisterRequest): Promise<void> {
  return request.post('/api/auth/register', data)
}

export function logout(): Promise<void> {
  return request.post('/api/auth/logout')
}


