<template>
  <div class="login-container">
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-icon">
          <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
            <circle cx="100" cy="100" r="95" fill="#10b981" opacity="0.1" stroke="#10b981" stroke-width="2"/>
            <path d="M 60 100 L 90 130 L 140 70" stroke="#10b981" stroke-width="4" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 class="brand-title">TradeLens</h1>
        <p class="brand-subtitle">闲置物品智选交易平台</p>
        <p class="brand-desc">安全可信的二手商品交易社区</p>
      </div>
    </div>

    <div class="login-form-container">
      <div class="form-card">
        <div class="mobile-logo">
           <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg" width="40" height="40">
             <path d="M 60 100 L 90 130 L 140 70" stroke="#10b981" stroke-width="10" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
           </svg>
           <span class="text">TradeLens</span>
        </div>

        <div class="header-group">
          <Transition name="fade-slide" mode="out-in">
            <h2 class="form-title" :key="isLogin ? 'login-t' : 'reg-t'">
              {{ isLogin ? '登录账户' : '创建账户' }}
            </h2>
          </Transition>
          <Transition name="fade-slide" mode="out-in">
            <p class="form-subtitle" :key="isLogin ? 'login-s' : 'reg-s'">
              {{ isLogin ? '欢迎回来' : '加入我们，开始交易' }}
            </p>
          </Transition>
        </div>

        <el-form ref="formRef" :model="formData" :rules="rules" @submit.prevent="handleSubmit">
          <el-form-item prop="username">
            <el-input
              v-model="formData.username"
              placeholder="用户名"
              clearable
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              placeholder="密码"
              clearable
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <Transition name="expand">
            <div v-if="!isLogin" class="expand-wrapper">
              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="formData.confirmPassword"
                  type="password"
                  placeholder="确认密码"
                  clearable
                  size="large"
                  show-password
                  :prefix-icon="Lock"
                />
              </el-form-item>
            </div>
          </Transition>

          <el-button
            type="primary"
            size="large"
            class="form-button"
            native-type="submit"
            :loading="loading"
          >
            {{ isLogin ? '登 录' : '注 册' }}
          </el-button>
        </el-form>

        <div class="form-toggle">
          <span>{{ isLogin ? '还没有账户？' : '已有账户？' }}</span>
          <el-link type="primary" :underline="false" @click="toggleMode" class="toggle-link">
            {{ isLogin ? '立即注册' : '返回登录' }}
          </el-link>
        </div>

        <div class="form-divider">
          <span>或者</span>
        </div>

        <div class="social-login">
          <el-button class="social-btn wechat" circle size="large" :icon="ChatDotRound" />
          <el-button class="social-btn qq" circle size="large" :icon="Postcard" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 模块化：导入部分
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { User, Lock, ChatDotRound, Postcard } from '@element-plus/icons-vue'

/**
 * 模块化：状态定义
 */
const router = useRouter()
const authStore = useAuthStore()

const isLogin = ref(true)      // 当前是否为登录模式
const loading = ref(false)     // 提交加载状态
const formRef = ref<FormInstance>()

// 表单数据模型
const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

/**
 * 模块化：验证规则
 */
const rules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, max: 20, message: '长度3-20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '长度至少6个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== formData.password) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

/**
 * 模块化：业务逻辑方法
 */

// 切换登录/注册模式
function toggleMode() {
  isLogin.value = !isLogin.value
  formRef.value?.resetFields()
}

// 表单提交处理
async function handleSubmit() {
  if (!formRef.value) return

  // 1. 表单校验
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      // 2. 根据模式执行对应接口
      if (isLogin.value) {
        await authStore.login({
            username: formData.username,
            password: formData.password
        })
        ElMessage.success('登录成功！')
        router.push('/')
      } else {
        await authStore.register({
          username: formData.username,
          password: formData.password
        })
        ElMessage.success('注册成功！请登录')
        // 注册成功后自动切换回登录并清空密码
        isLogin.value = true
        formData.password = ''
        formData.confirmPassword = ''
      }
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #f0fdf4 0%, #d1fae5 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

  .login-brand {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    background-image: radial-gradient(#10b981 1px, transparent 1px);
    background-size: 40px 40px;
    background-color: rgba(255, 255, 255, 0.3);

    .brand-content {
      text-align: center;
      max-width: 400px;

      .brand-icon {
        width: 120px;
        height: 120px;
        margin: 0 auto 30px;
        animation: float 6s ease-in-out infinite;
      }
      .brand-title {
        font-size: 48px;
        font-weight: 800;
        color: #065f46;
        margin-bottom: 10px;
        letter-spacing: -1px;
      }
      .brand-subtitle { font-size: 24px; color: #059669; margin-bottom: 15px; }
      .brand-desc { font-size: 16px; color: #047857; opacity: 0.8; }
    }
  }

  .login-form-container {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;

    .form-card {
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(10px);
      border-radius: 24px;
      padding: 48px;
      box-shadow: 0 20px 50px -12px rgba(16, 185, 129, 0.15); // 调淡阴影
      width: 100%;
      max-width: 420px;
      transition: all 0.3s ease;
      display: flex;
      flex-direction: column;

      .mobile-logo {
        display: none;
        align-items: center;
        justify-content: center;
        gap: 10px;
        margin-bottom: 24px;
        color: #10b981;
        font-weight: 700;
        font-size: 20px;
      }

      .header-group {
        margin-bottom: 24px;
        min-height: 80px; // 占位，防止切换时抖动太大
      }

      .form-title {
        font-size: 28px;
        font-weight: 700;
        color: #111827;
        margin-bottom: 8px;
      }
      .form-subtitle { color: #6b7280; }

      .el-form {
        :deep(.el-form-item) {
          margin-bottom: 24px; // 增加间距
          .el-input__wrapper {
            box-shadow: 0 0 0 1px #e5e7eb;
            padding: 8px 12px;
            background-color: #f9fafb;
            transition: all 0.2s;
            &.is-focus {
              box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2), 0 0 0 1px #10b981 !important; // 优化聚焦效果
              background-color: #fff;
            }
          }
        }
      }

      .form-button {
        width: 100%;
        height: 44px; // 稍微调小一点高度，更精致
        font-size: 16px;
        font-weight: 600;
        background: #10b981;
        border: none;
        border-radius: 8px; // 调小圆角
        box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.3);
        margin-top: 8px;
        
        &:hover {
          background: #059669;
          transform: translateY(-1px);
        }
        &:active { transform: translateY(0); }
      }

      .form-toggle {
        margin-top: 24px;
        text-align: center;
        font-size: 14px;
        color: #6b7280;
        .toggle-link {
          margin-left: 5px;
          font-weight: 600;
          color: #10b981;
          &:hover { color: #059669; }
        }
      }

      .form-divider {
        margin: 24px 0;
        position: relative;
        text-align: center;
        &::after {
          content: "";
          position: absolute;
          left: 0; top: 50%; width: 100%; height: 1px; background: #f3f4f6; z-index: 0;
        }
        span {
          background: #fff; 
          padding: 0 12px; 
          position: relative; 
          z-index: 1; 
          color: #9ca3af; 
          font-size: 12px;
        }
      }

      .social-login {
        display: flex;
        justify-content: center;
        gap: 16px;
        .social-btn {
          border-color: #f3f4f6;
          color: #9ca3af;
          transition: all 0.3s;
          &:hover {
             border-color: #10b981;
             color: #10b981;
             background-color: #ecfdf5;
          }
        }
      }
    }
  }

  @keyframes float {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-15px); }
  }

  // 过渡动画样式
  // 1. 展开/收起过渡 (用于确认密码框)
  .expand-enter-active,
  .expand-leave-active {
    transition: all 0.3s ease-in-out;
    max-height: 100px;
    opacity: 1;
    overflow: hidden;
  }
  .expand-enter-from,
  .expand-leave-to {
    max-height: 0;
    opacity: 0;
    margin-bottom: 0 !important; // 消除 margin 影响
    transform: translateY(-10px);
  }

  // 2. 文字淡入淡出位移 (用于标题)
  .fade-slide-enter-active,
  .fade-slide-leave-active {
    transition: all 0.25s ease;
  }
  .fade-slide-enter-from {
    opacity: 0;
    transform: translateX(10px);
  }
  .fade-slide-leave-to {
    opacity: 0;
    transform: translateX(-10px);
  }
}

// 响应式
@media (max-width: 768px) {
  .login-container {
    .login-brand { display: none; } 
    
    .login-form-container {
      background: #fff;
      padding: 0;
      
      .form-card {
        box-shadow: none;
        padding: 30px 24px;
        max-width: 100%;
        height: 100vh;
        border-radius: 0;
        justify-content: center;
        .mobile-logo { display: flex; }
      }
    }
  }
}
</style>