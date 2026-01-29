<script setup lang="ts">
import { RouterView } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <NavBar v-if="route.name !== 'login'" />
  
  <RouterView />
</template>

<style>
/* 全局重置样式，保证极简风格 */
body {
  margin: 0;
  background-color: #f9fafb; /* 浅灰背景 */
  color: #1f2937;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}
a { text-decoration: none; }
</style>