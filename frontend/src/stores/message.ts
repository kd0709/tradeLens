import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useAuthStore } from './auth';
import request from '@/api/request';

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0);
  const ws = ref<WebSocket | null>(null);

  // 初始化获取未读总数
  const fetchUnreadCount = async () => {
    try {
      const res: any = await request.get('/api/message/unread/count');
      // 拦截器已经脱壳，res 直接就是后端返回的数字 (比如 0)
      if (typeof res === 'number') {
        unreadCount.value = res;
      }
    } catch (error) {
      console.error('获取未读消息数失败', error);
    }
  };

  // 建立 WebSocket 连接
  const connectWs = () => {
    const authStore = useAuthStore();
    
   if (!authStore.user?.id) {
      console.error('WebSocket 连接失败：未获取到当前用户的 ID，请确认是否已登录');
      return;
    }
  
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host; 
    const wsUrl = `${protocol}//localhost:8080/api/ws/${authStore.user.id}`;
    console.log('正在连接 WebSocket:', wsUrl); // 新增日志
    
    


    ws.value = new WebSocket(wsUrl);

    ws.value.onopen = () => {
      console.log('WebSocket 连接成功');
      fetchUnreadCount(); 
    };

    ws.value.onmessage = (event) => {
      console.log('收到新消息:', event.data);
      unreadCount.value += 1;
    };

    ws.value.onclose = () => {
      console.log('WebSocket 连接关闭');
      ws.value = null;
    };

    ws.value.onerror = (error) => {
      console.error('WebSocket 发生错误', error);
    };
  };

  const disconnectWs = () => {
    if (ws.value) {
      ws.value.close();
      ws.value = null;
    }
    unreadCount.value = 0;
  };

  const clearUnread = () => {
    unreadCount.value = 0;
  };

  return {
    unreadCount,
    ws,
    fetchUnreadCount,
    connectWs,
    disconnectWs,
    clearUnread
  };
});