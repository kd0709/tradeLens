<template>
  <div class="login-container">
    <!-- 左侧品牌区 -->
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

    <!-- 右侧登录表单区 -->
    <div class="login-form-container">
      <div class="form-card">
        <h2 class="form-title">{{ isLogin ? '登录账户' : '创建账户' }}</h2>
        <p class="form-subtitle">{{ isLogin ? '欢迎回来' : '加入我们，开始交易' }}</p>

        <el-form ref="formRef" :model="formData" :rules="rules" @submit.prevent="handleSubmit">
          <!-- 用户名 -->
          <el-form-item prop="username">
            <el-input
              v-model="formData.username"
              placeholder="用户名"
              clearable
              size="large"
              prefix-icon="User"
            />
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              placeholder="密码"
              clearable
              size="large"
              show-password
              prefix-icon="Lock"
            />
          </el-form-item>

          <!-- 确认密码（注册时显示） -->
          <el-form-item v-if="!isLogin" prop="confirmPassword">
            <el-input
              v-model="formData.confirmPassword"
              type="password"
              placeholder="确认密码"
              clearable
              size="large"
              show-password
              prefix-icon="Lock"
            />
          </el-form-item>

          <!-- 记住密码（登录时显示） -->
          <div v-if="isLogin" class="form-remember">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" href="#">忘记密码？</el-link>
          </div>

          <!-- 提交按钮 -->
          <el-button
            type="primary"
            size="large"
            class="form-button"
            @click="handleSubmit"
            :loading="loading"
          >
            {{ isLogin ? '登 录' : '注 册' }}
          </el-button>
        </el-form>

        <!-- 切换模式 -->
        <div class="form-toggle">
          <span>{{ isLogin ? '还没有账户？' : '已有账户？' }}</span>
          <el-link type="primary" @click="toggleMode">
            {{ isLogin ? '立即注册' : '返回登录' }}
          </el-link>
        </div>

        <!-- 其他登录方式 -->
        <div class="form-divider">
          <span>或者</span>
        </div>

        <div class="social-login">
          <el-button class="social-btn wechat" circle size="large" icon="SuccessFilled" />
          <el-button class="social-btn qq" circle size="large" icon="Picture" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage, FormInstance } from 'element-plus'

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
    { min: 3, max: 20, message: '用户名长度必须为3-20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度至少6个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== formData.password) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

// 切换登录/注册模式
function toggleMode() {
  isLogin.value = !isLogin.value
  formRef.value?.clearValidate()
  formData.username = ''
  formData.password = ''
  formData.confirmPassword = ''
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true

  try {
    if (isLogin.value) {
      // 登录
      await authStore.login(formData.username, formData.password)
      ElMessage.success('登录成功！')
      router.push('/')
    } else {
      // 注册
      await authStore.register(
        formData.username,
        formData.password,
        formData.confirmPassword
      )
      ElMessage.success('注册成功！请登录')
      isLogin.value = true
      formData.password = ''
      formData.confirmPassword = ''
      formRef.value?.clearValidate()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 50%, #d1fae5 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;

  // 左侧品牌区
  .login-brand {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;

    .brand-content {
      text-align: center;
      max-width: 400px;

      .brand-icon {
        width: 120px;
        height: 120px;
        margin: 0 auto 30px;
        animation: float 3s ease-in-out infinite;

        svg {
          width: 100%;
          height: 100%;
        }
      }

      .brand-title {
        font-size: 48px;
        font-weight: 700;
        background: linear-gradient(135deg, #059669 0%, #10b981 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        margin: 0 0 10px 0;
      }

      .brand-subtitle {
        font-size: 20px;
        color: #047857;
        margin: 0 0 15px 0;
        font-weight: 600;
      }

      .brand-desc {
        font-size: 14px;
        color: #10b981;
        margin: 0;
        opacity: 0.8;
      }
    }
  }

  // 右侧表单区
  .login-form-container {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;

    .form-card {
      background: white;
      border-radius: 16px;
      padding: 50px 40px;
      box-shadow: 0 10px 40px rgba(16, 185, 129, 0.1);
      width: 100%;
      max-width: 400px;

      .form-title {
        font-size: 28px;
        font-weight: 700;
        color: #059669;
        margin: 0 0 5px 0;
      }

      .form-subtitle {
        font-size: 14px;
        color: #6b7280;
        margin: 0 0 30px 0;
      }

      .el-form {
        :deep(.el-form-item) {
          margin-bottom: 24px;

          .el-input {
            --el-input-focus-border-color: #10b981;
            --el-input-border-color: #d1fae5;

            &:hover {
              --el-input-border-color: #10b981;
            }
          }

          &.is-error {
            .el-input {
              --el-input-border-color: #ef4444;
            }
          }
        }
      }

      .form-remember {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;
        font-size: 14px;

        .el-checkbox {
          :deep(.el-checkbox__label) {
            color: #6b7280;
          }
        }

        .el-link {
          font-size: 14px;
        }
      }

      .form-button {
        width: 100%;
        height: 44px;
        font-size: 16px;
        font-weight: 600;
        background: linear-gradient(135deg, #059669 0%, #10b981 100%);
        border: none;
        border-radius: 8px;
        transition: all 0.3s ease;

        &:hover {
          background: linear-gradient(135deg, #047857 0%, #059669 100%);
          box-shadow: 0 6px 20px rgba(16, 185, 129, 0.3);
        }

        &:active {
          transform: scale(0.98);
        }

        :deep(.el-icon) {
          margin-right: 8px;
        }
      }

      .form-toggle {
        text-align: center;
        margin-top: 24px;
        font-size: 14px;
        color: #6b7280;

        span {
          margin-right: 8px;
        }

        .el-link {
          font-size: 14px;
        }
      }

      .form-divider {
        position: relative;
        text-align: center;
        margin: 30px 0;
        color: #d1d5db;
        font-size: 14px;

        &::before {
          content: '';
          position: absolute;
          top: 50%;
          left: 0;
          right: 0;
          height: 1px;
          background: #e5e7eb;
        }

        span {
          position: relative;
          background: white;
          padding: 0 16px;
        }
      }

      .social-login {
        display: flex;
        gap: 16px;
        justify-content: center;

        .social-btn {
          width: 48px;
          height: 48px;
          border: 1px solid #d1fae5;
          color: #10b981;
          transition: all 0.3s ease;

          &:hover {
            background: #f0fdf4;
            border-color: #10b981;
            color: #059669;
          }

          &.wechat:hover {
            background: #e7f5ee;
            border-color: #10b981;
          }

          &.qq:hover {
            background: #e7f5ee;
            border-color: #10b981;
          }
        }
      }
    }
  }

  // 动画
  @keyframes float {
    0%, 100% {
      transform: translateY(0px);
    }
    50% {
      transform: translateY(-20px);
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;

    .login-brand {
      display: none;
    }

    .login-form-container {
      padding: 20px;

      .form-card {
        padding: 40px 24px;
      }
    }
  }
}
</style>
