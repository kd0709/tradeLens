import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginRequest, RegisterRequest } from '../dto/auth' 
import * as authApi from '../api/auth'

export const useAuthStore = defineStore('auth', () => {

  const user = ref<UserInfo | null>(null)
  const token = ref<string>(localStorage.getItem('token') || '') // 直接初始化，简化 initAuth
  const isLoggedIn = computed(() => !!token.value)


  async function login(loginData: LoginRequest) {
    const result = await authApi.login(loginData)
    
    // 假设后端返回结构为 { token: string, user: UserInfo }
    // 如果 result 直接就是 UserInfo 且包含 token，则按需提取
    token.value = result.token
    user.value = result 

    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result))
  }

  /**
   * 注册
   */
  async function register(registerData: RegisterRequest) {
    await authApi.register(registerData)
  }

  /**
   * 登出
   */
  async function logout() {
    try {
      await authApi.logout()
    } catch (err) {
      console.error('登出接口调用失败', err)
    } finally {
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

  /**
   * 初始化时从本地恢复用户信息
   */
  function initAuth() {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (e) {
        localStorage.removeItem('user')
      }
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    initAuth,
    login,
    register,
    logout,
  }
})