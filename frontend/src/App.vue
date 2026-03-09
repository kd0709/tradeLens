<script setup lang="ts">
import { RouterView } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <NavBar v-if="route.name !== 'login'" />
  
  <RouterView v-slot="{ Component }">
    <Transition name="zoom-fade" mode="out-in">
      <keep-alive include="Home">
        <component :is="Component" :key="route.fullPath" />
      </keep-alive>
    </Transition>
  </RouterView>

</template>

<style>
/* 全局重置样式，保证极简风格 */
body {
  margin: 0;
  background-color: #f9fafb; /*浅背景 */
  color: #1f2937;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  overflow-x: hidden;
}
a { text-decoration: none; }

/* 页面动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.3s ease-out;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/*淡入放大动画 */
.zoom-fade-enter-active,
.zoom-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.zoom-fade-enter-from {
  opacity: 0;
  transform: scale(0.95);
}

.zoom-fade-leave-to {
  opacity: 0;
  transform: scale(1.05);
}

/*按钮加载状态样式 */
.el-button.loading::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 16px;
  height: 16px;
  margin: -8px 0 0 -8px;
  border: 2px solid transparent;
  border-top-color: currentColor;
  border-radius: 50%;
  animation: button-spin 1s linear infinite;
}

@keyframes button-spin {
  to { transform: rotate(360deg); }
}

/* 悬浮按钮效果 */
.btn-hover-pop {
  position: relative;
  transform-origin: center;
  animation: button-hover 0.3s ease;
}

@keyframes button-hover {
  0% { transform: translateZ(0); }
  50% { transform: translate3d(0, -2px, 0) scale(1.02); }
  100% { transform: translateZ(0); }
}
</style>