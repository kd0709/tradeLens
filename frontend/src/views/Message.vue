<template>
  <div class="message-container">
    <el-card class="chat-card" body-style="padding: 0; display: flex; height: 100%;">
      
      <div class="contact-sidebar">
        <div class="sidebar-header">最近联系人</div>
        <div class="contact-list">
          <div 
            v-for="c in contacts" 
            :key="c.id"
            :class="['contact-item', { active: currentTarget?.id === c.id }]"
            @click="selectContact(c)"
          >
            <el-badge :value="c.unread" :hidden="!c.unread || c.unread === 0">
              <el-avatar :size="40" :src="getFullImageUrl(c.avatar) || defaultAvatar" />
            </el-badge>
            <span class="contact-name">{{ c.nickname }}</span>
          </div>
        </div>
      </div>

      <div class="chat-main">
        <template v-if="currentTarget">
          <div class="chat-header">
            {{ currentTarget.id === 0 ? '系统通知' : `与 ${currentTarget.nickname} 的对话` }}
          </div>
          
          <div class="message-list" id="msg-list">
            <div 
              v-for="msg in historyList" 
              :key="msg.id" 
              :class="['msg-bubble', msg.senderId === authStore.user?.id ? 'msg-right' : 'msg-left']"
            >
              <div class="msg-content">{{ msg.content }}</div>
              <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
            </div>
            <div v-if="historyList.length === 0" class="empty-tip">暂无聊天记录，打个招呼吧~</div>
          </div>

          <div class="input-area" v-if="currentTarget.id !== 0">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="3"
              resize="none"
              placeholder="请输入消息内容，按 回车 发送"
              @keyup.enter.prevent="sendMessage"
            />
            <div class="send-action">
              <el-button type="primary" @click="sendMessage">发送消息</el-button>
            </div>
          </div>
          <div class="input-area blocked" v-else>
            <el-alert title="系统通知消息不支持回复" type="info" center :closable="false" />
          </div>
        </template>

        <div v-else class="no-chat-selected">
          <el-empty description="请选择左侧联系人开始聊天" />
        </div>
      </div>

    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import { getMessageHistory, markMessageRead, getMessageContacts } from '@/api/message'
import { ElMessage } from 'element-plus'
import { getFullImageUrl } from '@/utils/image'

const route = useRoute()
const authStore = useAuthStore()
const messageStore = useMessageStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const contacts = ref<any[]>([])
const currentTarget = ref<any>(null)
const historyList = ref<any[]>([])
const inputText = ref('')

const initContacts = async () => {
  try {
    const res: any = await getMessageContacts()
    contacts.value = res || []

    const targetId = Number(route.query.targetId)
    if (!isNaN(targetId) && targetId > 0) {
      let existContact = contacts.value.find(c => c.id === targetId)
      
      if (!existContact) {
        existContact = {
          id: targetId,
          nickname: route.query.nickname || '新联系人',
          avatar: route.query.avatar || '',
          unread: 0
        }
        contacts.value.unshift(existContact)
      }
      selectContact(existContact)
    } else if (contacts.value.length > 0) {
      // 若没有目标ID且列表不为空，默认选中第一个人
      selectContact(contacts.value[0])
    }
  } catch (error) {
    console.error('获取联系人失败', error)
  }
}

const handleRouteQuery = () => {
  const targetId = Number(route.query.targetId)
  
  if (targetId && targetId > 0) {
    // 使用双等号 == 进行弱类型比较，防止后端传回来的 id 是字符串类型
    let existContact = contacts.value.find(c => c.id == targetId)
    
    if (!existContact) {
      // 列表中没找到这个人，强制创建一个临时联系人放到最前面
      existContact = {
        id: targetId,
        nickname: route.query.nickname || '新联系人',
        avatar: route.query.avatar || '',
        unread: 0
      }
      contacts.value.unshift(existContact)
    }
    // 选中他
    selectContact(existContact)
  } else if (contacts.value.length > 0 && !currentTarget.value) {
    // 只有在没带 targetId 的情况下，才默认选中列表第一个人（通常是系统通知）
    selectContact(contacts.value[0])
  }
}

// 监听路由参数变化（解决已经在聊天页面内，再次通过其他途径跳转时的不刷新问题）
watch(
  () => route.query.targetId,
  (newVal, oldVal) => {
    if (newVal !== oldVal && contacts.value.length > 0) {
      handleRouteQuery()
    }
  }
)

const selectContact = async (contact: any) => {
  currentTarget.value = contact
  contact.unread = 0 
  try {
    const res: any = await getMessageHistory(contact.id)
    historyList.value = res || []
    scrollToBottom()
    
    await markMessageRead(contact.id)
    messageStore.fetchUnreadCount() 
  } catch (error) {
    console.error('获取历史消息失败', error)
  }
}

const sendMessage = () => {
  if (!inputText.value.trim() || !currentTarget.value) return
  
  console.log(messageStore.ws)
  console.log(messageStore.ws?.readyState)
  if (!messageStore.ws || messageStore.ws.readyState !== WebSocket.OPEN) {
    console.log('尝试重新建立 WebSocket 连接...');
    messageStore.connectWs();
    ElMessage.warning('网络正在重新连接，请重试');
    return
  }

  const msgObj = {
    receiverId: currentTarget.value.id,
    content: inputText.value.trim()
  }

  messageStore.ws.send(JSON.stringify(msgObj))

  historyList.value.push({
    id: Date.now(),
    senderId: authStore.user?.id,
    receiverId: currentTarget.value.id,
    content: msgObj.content,
    createTime: new Date().toISOString()
  })

  inputText.value = ''
  scrollToBottom()
}

watch(() => messageStore.unreadCount, () => {
  if (currentTarget.value) {
    selectContact(currentTarget.value)
  } else {
    initContacts()
  }
})

const scrollToBottom = () => {
  nextTick(() => {
    const box = document.getElementById('msg-list')
    if (box) box.scrollTop = box.scrollHeight
  })
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}

onMounted(() => {
  // 【新增】：如果进入页面时发现 ws 是 null，立刻连接
  if (!messageStore.ws) {
    messageStore.connectWs()
  }
  initContacts()
})
</script>

<style scoped lang="scss">
.message-container {
  max-width: 1000px;
  margin: 20px auto;
  height: calc(100vh - 120px);
}
.chat-card {
  height: 100%;
}

.contact-sidebar {
  width: 260px;
  background: #fafafa;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;

  .sidebar-header {
    height: 60px;
    line-height: 60px;
    padding: 0 20px;
    font-size: 16px;
    font-weight: bold;
    border-bottom: 1px solid #ebeef5;
    background: #fff;
  }

  .contact-list {
    flex: 1;
    overflow-y: auto;

    .contact-item {
      display: flex;
      align-items: center;
      padding: 12px 20px;
      cursor: pointer;
      transition: background 0.2s;

      &:hover {
        background: #f0f2f5;
      }
      &.active {
        background: #e6f7ff;
        border-right: 3px solid #1890ff;
      }

      .contact-name {
        margin-left: 12px;
        font-size: 15px;
        color: #333;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;

  .chat-header {
    height: 60px;
    line-height: 60px;
    padding: 0 20px;
    font-size: 16px;
    font-weight: bold;
    border-bottom: 1px solid #ebeef5;
  }

  .message-list {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    background-color: #f5f7fa;

    .empty-tip {
      text-align: center;
      color: #999;
      margin-top: 50px;
    }

    .msg-bubble {
      margin-bottom: 20px;
      display: flex;
      flex-direction: column;
      
      &.msg-left { align-items: flex-start; }
      &.msg-right { align-items: flex-end; }

      .msg-content {
        max-width: 60%;
        padding: 10px 14px;
        border-radius: 8px;
        background-color: white;
        box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        word-break: break-all;
        font-size: 14px;
        line-height: 1.5;
      }

      &.msg-right .msg-content {
        background-color: #95ec69; /* 微信绿 */
      }

      .msg-time {
        font-size: 12px;
        color: #999;
        margin-top: 6px;
      }
    }
  }

  .input-area {
    padding: 15px;
    border-top: 1px solid #ebeef5;
    background: #fff;
 
    :deep(.el-textarea__inner) {
      border: none;
      box-shadow: none;
      &:focus { box-shadow: none; }
    }

    .send-action {
      margin-top: 10px;
      text-align: right;
    }

    &.blocked {
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }

  .no-chat-selected {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style>