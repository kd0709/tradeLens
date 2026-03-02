import { ElMessage, ElNotification } from 'element-plus'

// 成功消息
export const showSuccess = (message: string, duration: number = 3000) => {
  ElMessage.success({
    message,
    duration,
    showClose: true,
    grouping: true
  })
}

//错误消息
export const showError = (message: string, duration: number = 5000) => {
  ElMessage.error({
    message,
    duration,
    showClose: true,
    grouping: true
  })
}

//警消息
export const showWarning = (message: string, duration: number = 4000) => {
  ElMessage.warning({
    message,
    duration,
    showClose: true,
    grouping: true
  })
}

// 信息消息
export const showInfo = (message: string, duration: number = 3000) => {
  ElMessage.info({
    message,
    duration,
    showClose: true,
    grouping: true
  })
}

// 通知消息
export const showNotification = (title: string, message: string, type: 'success' | 'warning' | 'info' | 'error' = 'info') => {
  ElNotification({
    title,
    message,
    type,
    duration: 5000,
    showClose: true
  })
}