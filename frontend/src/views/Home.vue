<template>
  <div class="home-container">
    <el-header class="home-header">
      <div class="header-left">
        <h1 class="logo">TradeLens</h1>
      </div>
      <div class="header-right">
        <span class="user-info">欢迎，{{ authStore.user?.nickname }}</span>
        <el-button type="primary" text @click="handleLogout">登出</el-button>
      </div>
    </el-header>
    
    <el-container class="main-container">
      <el-aside width="200px" class="sidebar">
        <el-menu default-active="1" class="el-menu-vertical">
          <el-menu-item index="1">
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="2">
            <span>我的商品</span>
          </el-menu-item>
          <el-menu-item index="3">
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="4">
            <span>我的收藏</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main-content">
        <h2>欢迎使用 TradeLens 平台</h2>
        <p>这是一个安全可信的闲置物品交易社区</p>
        <p>用户信息: {{ authStore.user }}</p>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

async function handleLogout() {
  try {
    await authStore.logout()
    ElMessage.success('登出成功')
    router.push('/login')
  } catch (error: any) {
    ElMessage.error(error.message || '登出失败')
  }
}
</script>

<style scoped lang="scss">
.home-container {
  height: 100vh;
  background: #f5f7fa;

  .home-header {
    background: linear-gradient(135deg, #059669 0%, #10b981 100%);
    color: white;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    box-shadow: 0 2px 8px rgba(16, 185, 129, 0.1);

    .header-left {
      .logo {
        font-size: 24px;
        font-weight: 700;
        margin: 0;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 20px;

      .user-info {
        font-size: 14px;
      }
    }
  }

  .main-container {
    height: calc(100vh - 60px);

    .sidebar {
      background: white;
      border-right: 1px solid #e5e7eb;

      :deep(.el-menu) {
        border-right: none;

        .el-menu-item {
          color: #6b7280;
          
          &:hover {
            background: #f0fdf4 !important;
            color: #10b981;
          }

          &.is-active {
            background: #f0fdf4 !important;
            color: #10b981;
            border-right: 3px solid #10b981;
          }
        }
      }
    }

    .main-content {
      background: white;
      padding: 30px;

      h2 {
        color: #059669;
        margin-top: 0;
      }

      p {
        color: #6b7280;
      }
    }
  }
}
</style>
