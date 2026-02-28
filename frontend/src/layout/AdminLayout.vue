<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="aside">
      <div class="logo">
        <el-icon class="logo-icon"><Leaf /></el-icon>
        闲置平台管理
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="admin-menu"
        background-color="#ffffff"
        text-color="#606266"
        active-text-color="#10b981"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Box /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-right">
          <span class="admin-name">管理员：{{ authStore.user?.nickname || 'Admin' }}</span>
          <el-button type="danger" link @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
// 补全缺失的图标引入，并加入 Leaf 图标用于 Logo
import { DataBoard, User, Goods, Box, List, ChatDotRound,  } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background-color: #ffffff;
  box-shadow: 2px 0 8px 0 rgba(29, 35, 41, 0.05); /* 增加轻盈的阴影 */
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #10b981; /* 清爽绿 */
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.logo-icon {
  font-size: 20px;
}

.admin-menu {
  border-right: none;
  flex: 1;
}

/* 自定义菜单选中状态，浅绿背景色块 + 绿边增强视觉 */
:deep(.el-menu-item.is-active) {
  background-color: #ecfdf5 !important;
  border-right: 3px solid #10b981;
  font-weight: 500;
}

:deep(.el-menu-item:hover) {
  background-color: #f3fdf8 !important;
}

.header {
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.04);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
  z-index: 9;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.admin-name {
  color: #606266;
  font-size: 14px;
}

.main-content {
  background-color: #f4f7f6; /* 带有极其微弱绿意的护眼浅灰 */
  padding: 20px;
}

.content-wrapper {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px;
  min-height: calc(100vh - 100px);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}
</style>