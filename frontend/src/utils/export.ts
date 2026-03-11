import request from '@/api/request'
import { ElMessage } from 'element-plus'

/**
 * 通用 Excel 导出方法
 * @param url 后端导出接口地址
 * @param filename 下载保存的文件名 (包含 .xlsx)
 * @param params 附加的查询参数 (可选)
 */
export async function exportExcel(url: string, filename: string, params?: any) {
  try {
    const res: any = await request.get(url, {
      params,
      responseType: 'blob'
    })
    
    // 我们已经在 request.ts 拦截器中放行了 blob
    const blobData = res.data ? res.data : res
    const blob = new Blob([blobData], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    
    const objectUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    
    document.body.removeChild(link)
    window.URL.revokeObjectURL(objectUrl)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败，请重试')
  }
}