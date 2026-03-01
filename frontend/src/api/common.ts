import request from './request'

export function uploadFile(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then((res: any) => {
    return typeof res === 'string' ? res : (res.url || res)
  })
}

export const aiAutoFill = (imageUrl: string) => {
  return request.get('/api/common/ai/fill-form', { params: { imageUrl } })
}
