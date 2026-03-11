import request from './request'

// 获取与某人的历史聊天记录 (targetId=0 为系统通知)
export function getMessageHistory(targetId: number) {
  return request.get(`/api/message/history/${targetId}`)
}

// 将某人发给我的消息标为已读
export function markMessageRead(senderId: number) {
  return request.put(`/api/message/read/${senderId}`)
}

// 获取最近联系人列表
export function getMessageContacts() {
  return request.get('/api/message/contacts')
}