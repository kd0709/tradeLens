import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  // 滚动行为：跳转新页面时自动回到顶部
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/product/:id',
      name: 'ProductDetail',
      component: () => import('../views/ProductDetail.vue')
    },
    {
      path: '/publish',
      name: 'Publish',
      component: () => import('../views/Publish.vue'),
      meta: { requiresAuth: true } 
    },
    {
      path: '/user',
      name: 'UserCenter',
      component: () => import('../views/UserCenter.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/order/create',
      name: 'OrderCreate',
      component: () => import('../views/OrderCreate.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/pay/:id',
      name: 'Pay',
      component: () => import('../views/Pay.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/cart',
      name: 'Cart',
      component: () => import('../views/Cart.vue'),
      meta: { requiresAuth: true }
    },
  ]
})

// 简单的全局路由守卫（检查需要登录的页面）
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token') //  token 存在 localStorage
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router