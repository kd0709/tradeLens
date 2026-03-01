import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginRequest, RegisterRequest } from '../dto/auth' 
import * as authApi from '../api/auth'

export const useAuthStore = defineStore('auth', () => {

  const user = ref<UserInfo | null>(null)
  const token = ref<string>(localStorage.getItem('token') || '')
  const isLoggedIn = computed(() => !!token.value)

  async function login(loginData: LoginRequest) {
    const result = await authApi.login(loginData)
    token.value = result.token
    user.value = result
    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result))
  }

  async function register(registerData: RegisterRequest) {
    await authApi.register(registerData)
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
    } finally {
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

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