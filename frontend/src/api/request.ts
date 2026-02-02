import axios ,{ AxiosError} from 'axios'
import type { AxiosInstance } from 'axios'

import { ElMessage } from 'element-plus'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const request: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
})



// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response
    
    // 检查业务响应码
    if (data.code === 200) {
      return data.data
    } else if (data.code === 401) {
      // Token无效或过期
      localStorage.removeItem('token')
      window.location.href = '/login'
      ElMessage.error('登录过期，请重新登录')
      return Promise.reject(new Error(data.message))
    } else {
      ElMessage.error(data.message || '操作失败')
      return Promise.reject(new Error(data.message))
    }
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
      ElMessage.error('登录过期，请重新登录')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
