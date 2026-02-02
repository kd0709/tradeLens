import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { useAuthStore } from './stores/auth'


import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)


// 在挂载前初始化权限状态
const authStore = useAuthStore()
authStore.initAuth()

app.mount('#app')
