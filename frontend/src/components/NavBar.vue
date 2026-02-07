<template>
  <nav class="navbar">
    <div class="container">
      <router-link to="/" class="logo">
        <span class="icon">♻️</span>
        <span class="text">TradeLens</span>
      </router-link>

      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索好物..."
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="actions">
        <router-link to="/publish">
          <el-button type="primary" round class="publish-btn">发布闲置</el-button>
        </router-link>
        
        <div v-if="authStore.user" class="user-menu">
           <router-link to="/cart" class="icon-link">
             <el-badge :value="0" hidden class="badge">
               <el-icon><ShoppingCart /></el-icon>
             </el-badge>
           </router-link>
           
           <el-dropdown trigger="click">
            <div class="avatar-wrapper">
              <el-avatar :size="36" :src="getFullImageUrl(authStore.user.avatar) || defaultAvatar" />
              <span class="username">{{ authStore.user.nickname }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/user')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <div v-else class="auth-links">
          <router-link to="/login">登录</router-link>
          <span class="divider">/</span>
          <router-link to="/login">注册</router-link>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Search, ShoppingCart } from '@element-plus/icons-vue'
import { getFullImageUrl } from '@/utils/image'

const router = useRouter()
const authStore = useAuthStore()
const keyword = ref('')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/', query: { q: keyword.value } })
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.navbar {
  height: 64px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #f3f4f6;
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
    padding: 0 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .logo {
    display: flex;
    align-items: center;
    text-decoration: none;
    gap: 8px;
    
    .icon { font-size: 24px; }
    .text {
      font-size: 20px;
      font-weight: 700;
      color: #111827;
      letter-spacing: -0.5px;
    }
  }

  .search-bar {
    flex: 1;
    max-width: 400px;
    margin: 0 40px;

    :deep(.el-input__wrapper) {
      border-radius: 999px;
      background-color: #f3f4f6;
      box-shadow: none;
      padding-left: 16px;
      
      &.is-focus {
        background-color: #fff;
        box-shadow: 0 0 0 1px #10b981;
      }
    }
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 24px;

    .publish-btn {
      background-color: #10b981;
      border-color: #10b981;
      font-weight: 600;
      
      &:hover {
        background-color: #059669;
      }
    }

    .icon-link {
      color: #6b7280;
      font-size: 20px;
      transition: color 0.2s;
      
      &:hover { color: #10b981; }
    }

    .user-menu {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .avatar-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        
        .username {
          font-size: 14px;
          color: #374151;
          font-weight: 500;
        }
      }
    }

    .auth-links {
      font-size: 14px;
      
      a {
        color: #374151;
        text-decoration: none;
        font-weight: 600;
        &:hover { color: #10b981; }
      }
      .divider { margin: 0 8px; color: #d1d5db; }
    }
  }
}
</style>