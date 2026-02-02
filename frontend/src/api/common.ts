import request from './request'

// 上传图片
export function uploadFile(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  
  // 后端返回UploadDto，包含url字段
  return request.post('/api/common/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then((res: any) => {
    // 如果后端返回的是对象，提取url字段
    return typeof res === 'string' ? res : (res.url || res)
  })
}