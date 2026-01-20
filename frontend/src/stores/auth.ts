import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { UserInfo } from '../dto/auth'
import * as authApi from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref<UserInfo | null>(null)
  const token = ref<string>('')
  const isLoggedIn = computed(() => !!token.value)

  // 初始化 - 从localStorage恢复
  function initAuth() {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    
    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
    }
  }

  // 登录
  async function login(username: string, password: string) {
    const result = await authApi.login({ username, password })
    token.value = result.token
    user.value = result
    
    // 保存到localStorage
    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result))
  }

  // 注册
  async function register(username: string, password: string, confirmPassword: string) {
    await authApi.register({ username, password, confirmPassword })
  }

  // 登出
  async function logout() {
    try {
      await authApi.logout()
    } finally {
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

  // 更新用户信息
  function setUser(userInfo: UserInfo) {
    user.value = userInfo
    localStorage.setItem('user', JSON.stringify(userInfo))
  }

  return {
    user,
    token,
    isLoggedIn,
    initAuth,
    login,
    register,
    logout,
    setUser,
  }
})
