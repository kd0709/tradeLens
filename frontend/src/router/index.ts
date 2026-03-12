import { createRouter, createWebHistory } from 'vue-router'

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
      component: () => import('../views/Home.vue')
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
    {
      path: '/messages',
      name: 'Messages',
      component: () => import('@/views/Message.vue'),
      meta: { requiresAuth: true }
    },
    // 后台管理路由组
    {
      path: '/admin',
      component: () => import('../layout/AdminLayout.vue'),
      redirect: '/admin/dashboard',
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '数据看板' }
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/UserManage.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'categories',
          name: 'AdminCategories',
          component: () => import('@/views/admin/CategoryManage.vue'),
          meta: { title: '分类管理' }
        },
        {
          path: 'products',
          name: 'AdminProducts',
          component: () => import('@/views/admin/ProductManage.vue'),
          meta: { title: '商品管理' }
        },
        {
          path: 'orders',
          name: 'AdminOrders',
          component: () => import('@/views/admin/OrderManage.vue'),
          meta: { title: '订单管理' }
        },
        {
          path: 'comments',
          name: 'AdminComments',
          component: () => import('@/views/admin/CommentManage.vue'),
          meta: { title: '评价管理' }
        },
        {
          path: 'audit',
          name: 'ProductAudit',
          component: () => import('@/views/admin/ProductAudit.vue'),
          meta: { title: '商品审核中心' }
        }
      ]
    }




  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token') 
  const userStr = localStorage.getItem('user')
  
  // 新增：尝试解析本地存储的用户信息
  let user = null
  if (userStr) {
    try {
      user = JSON.parse(userStr)
    } catch (e) {
      console.error('Failed to parse user from localStorage', e)
    }
  }

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresAdmin && user?.role !== 1) {
    next('/')
  } else {
    next()
  }
})

export default router