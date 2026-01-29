import request from './request'

// 上传图片
export function uploadFile(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  
  // 注意：这里返回的是 data 字段，假设后端直接返回图片 URL 字符串
  // 如果后端返回结构是 { code: 200, data: "url" }，request 拦截器已经解包了 data
  return request.post('/api/common/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}