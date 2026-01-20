import { createRouter, createWebHistory, RouteRecordRaw, NavigationGuardNext } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Login from '../views/Login.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 初始化认证状态
  if (!authStore.user) {
    authStore.initAuth()
  }

  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !authStore.isLoggedIn) {
    // 需要登录但未登录，重定向到登录页
    next('/login')
  } else if (to.path === '/login' && authStore.isLoggedIn) {
    // 已登录但要访问登录页，重定向到首页
    next('/')
  } else {
    next()
  }
})

export default router
