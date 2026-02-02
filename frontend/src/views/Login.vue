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

        <h2 class="form-title">{{ isLogin ? '登录账户' : '创建账户' }}</h2>
        <p class="form-subtitle">{{ isLogin ? '欢迎回来' : '加入我们，开始交易' }}</p>

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

          <el-form-item v-if="!isLogin" prop="confirmPassword">
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

          <div v-if="isLogin" class="form-remember">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>

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
          <el-link type="primary" :underline="false" @click="toggleMode">
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { User, Lock, ChatDotRound, Postcard } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const isLogin = ref(true)
const loading = ref(false)
const rememberMe = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

// 验证规则
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

function toggleMode() {
  isLogin.value = !isLogin.value
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return

  // 前端表单校验
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isLogin.value) {
        // 登录业务
        await authStore.login({
            username: formData.username,
            password: formData.password
        })
        ElMessage.success('登录成功！')
        router.push('/')
      } else {
        // 注册业务
        await authStore.register({
          username: formData.username,
          password: formData.password
        })
        
        ElMessage.success('注册成功！请登录')
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
  // 优化渐变背景，更加柔和
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
    background-color: rgba(255, 255, 255, 0.3); // 增加一点纹理

    .brand-content {
      text-align: center;
      max-width: 400px;

      .brand-icon {
        width: 120px;
        height: 120px;
        margin: 0 auto 30px;
        animation: float 6s ease-in-out infinite; // 放慢动画更优雅
      }
      .brand-title {
        font-size: 48px;
        font-weight: 800;
        color: #065f46; // 使用实色代替渐变文字，兼容性更好
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
      background: rgba(255, 255, 255, 0.95); // 轻微透明
      backdrop-filter: blur(10px); // 毛玻璃效果
      border-radius: 24px; // 更大的圆角
      padding: 48px;
      box-shadow: 0 20px 50px -12px rgba(16, 185, 129, 0.25); // 优化阴影
      width: 100%;
      max-width: 420px;
      transition: all 0.3s ease;

      .mobile-logo {
        display: none; // 默认隐藏
        align-items: center;
        justify-content: center;
        gap: 10px;
        margin-bottom: 24px;
        color: #10b981;
        font-weight: 700;
        font-size: 20px;
      }

      .form-title {
        font-size: 28px;
        font-weight: 700;
        color: #111827; // 深色标题更易读
        margin-bottom: 8px;
      }
      .form-subtitle { color: #6b7280; margin-bottom: 32px; }

      .el-form {
        :deep(.el-form-item) {
          margin-bottom: 20px;
          .el-input__wrapper {
            box-shadow: 0 0 0 1px #e5e7eb; // 默认边框更淡
            padding: 8px 12px;
            background-color: #f9fafb; // 极淡的灰色背景
            &.is-focus {
              box-shadow: 0 0 0 1px #10b981 !important;
              background-color: #fff;
            }
          }
        }
      }

      .form-button {
        width: 100%;
        height: 48px;
        font-size: 16px;
        font-weight: 600;
        background: #10b981;
        border: none;
        border-radius: 12px;
        box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.4);
        
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
      }

      .form-divider {
        margin: 30px 0;
        position: relative;
        text-align: center;
        &::after {
          content: "";
          position: absolute;
          left: 0; top: 50%; width: 100%; height: 1px; background: #e5e7eb; z-index: 0;
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
        gap: 20px;
        .social-btn {
          border-color: #e5e7eb;
          color: #6b7280;
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
}

// 响应式优化
@media (max-width: 768px) {
  .login-container {
    .login-brand { display: none; } // 隐藏左侧大图
    
    .login-form-container {
      background: #fff; // 移动端背景纯白
      padding: 0;
      
      .form-card {
        box-shadow: none; // 去掉阴影
        padding: 30px 24px;
        max-width: 100%;
        height: 100vh; // 全屏
        border-radius: 0;
        display: flex;
        flex-direction: column;
        justify-content: center;

        .mobile-logo { display: flex; } // 显示顶部小Logo
      }
    }
  }
}
</style>