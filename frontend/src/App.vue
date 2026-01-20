<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()

onMounted(() => {
  authStore.initAuth()
})
</script>

<template>
  <router-view></router-view>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue',
    Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  width: 100%;
  height: 100%;
}

/* 绿色主题主色 */
:root {
  --el-color-primary: #10b981;
  --el-color-primary-light-3: #d1fae5;
  --el-color-primary-light-5: #a7f3d0;
  --el-color-primary-light-7: #6ee7b7;
  --el-color-primary-light-8: #34d399;
  --el-color-primary-light-9: #10b981;
  --el-color-success: #10b981;
}
</style>
