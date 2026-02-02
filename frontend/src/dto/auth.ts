export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
}


export interface UserInfo {
  username: string
  nickname: string
  phone?: string
  email?: string
  avatar?: string
  token: string
}
