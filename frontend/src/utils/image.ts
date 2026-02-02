import { API_BASE_URL } from '@/api/request' // 确保 request.ts 中导出了 API_BASE_URL

/**
 * 获取完整的图片 URL
 * @param url 后端返回的图片路径
 * @param defaultImg 默认占位图(可选)
 */
export const getFullImageUrl = (url?: string, defaultImg?: string) => {
  const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  
  if (!url) {
    return defaultImg || DEFAULT_AVATAR
  }

  // 如果是 http 开头，说明已经是绝对路径 (OSS模式)，直接返回
  if (url.startsWith('http') || url.startsWith('https')) {
    return url
  }

  // 否则拼接本地后端地址 (Local模式)
  // 处理 url 开头可能没有 / 的情况
  const path = url.startsWith('/') ? url : `/${url}`
  return `${API_BASE_URL}${path}`
}