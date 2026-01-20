// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 注册请求
export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
}

// 用户信息
export interface UserInfo {
  username: string
  nickname: string
  phone?: string
  email?: string
  avatar?: string
  token: string
}
