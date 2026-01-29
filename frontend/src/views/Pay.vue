<template>
  <div class="pay-page">
    <div class="container">
      <div class="pay-card">
        <div class="header">
          <div class="status-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="status-text">
            <h2>订单提交成功！去付款咯～</h2>
            <p>请在 <span class="countdown">15分钟</span> 内完成支付，超时将自动取消</p>
          </div>
          <div class="amount">
            <span class="symbol">¥</span>
            <span class="num">{{ order?.totalPrice || '0.00' }}</span>
          </div>
        </div>

        <div class="order-info" v-if="order">
          <div class="row">
            <span class="label">订单编号：</span>
            <span class="val">{{ order.orderNo }}</span>
          </div>
          <div class="row">
            <span class="label">收货信息：</span>
            <span class="val">{{ order.receiverName }} {{ order.receiverPhone }}</span>
          </div>
          <div class="row">
            <span class="label">商品名称：</span>
            <span class="val">{{ order.productTitle || '闲置商品' }}</span>
          </div>
        </div>

        <div class="payment-method">
          <div class="method-title">选择支付方式</div>
          <div class="methods">
            <div 
              class="method-item" 
              :class="{ active: payType === 'wechat' }"
              @click="payType = 'wechat'"
            >
              <img src="https://img.icons8.com/color/48/weixing.png" alt="微信支付"/>
              <span>微信支付</span>
              <el-icon class="check" v-if="payType === 'wechat'"><Select /></el-icon>
            </div>
            <div 
              class="method-item" 
              :class="{ active: payType === 'alipay' }"
              @click="payType = 'alipay'"
            >
              <img src="https://img.icons8.com/color/48/alipay.png" alt="支付宝"/>
              <span>支付宝</span>
              <el-icon class="check" v-if="payType === 'alipay'"><Select /></el-icon>
            </div>
          </div>
        </div>

        <div class="qr-section">
          <div class="qr-box">
             <div class="qr-placeholder" :class="payType">
               <el-icon :size="40" color="#fff"><Money /></el-icon>
             </div>
             <p>打开{{ payType === 'wechat' ? '微信' : '支付宝' }}扫一扫</p>
          </div>
        </div>

        <div class="actions">
           <el-button @click="router.push('/user')">稍后支付</el-button>
           <el-button type="primary" :loading="paying" @click="handleConfirmPay">
             模拟支付成功
           </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Timer, Select, Money } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getOrderDetail, payOrder } from '@/api/order'
import type { OrderDto } from '@/dto/order'

const route = useRoute()
const router = useRouter()
const payType = ref<'wechat' | 'alipay'>('wechat')
const order = ref<OrderDto | null>(null)
const paying = ref(false)

// 加载订单信息
const loadOrder = async () => {
  const id = Number(route.params.id)
  if (!id) return
  try {
    order.value = await getOrderDetail(id)
    if (order.value.orderStatus !== 1) {
      ElMessage.warning('该订单无需支付')
      router.replace('/user')
    }
  } catch (e) { console.error(e) }
}

// 确认支付
const handleConfirmPay = async () => {
  if (!order.value) return
  paying.value = true
  
  // 模拟网络延迟 1秒
  setTimeout(async () => {
    try {
      await payOrder(order.value!.id)
      ElMessage.success('支付成功！')
      router.replace('/user') // 回到用户中心查看状态
    } catch (e) {
      console.error(e)
    } finally {
      paying.value = false
    }
  }, 1000)
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped lang="scss">
.pay-page {
  background: #f9fafb;
  min-height: calc(100vh - 64px);
  padding: 40px 20px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

.pay-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}

.header {
  text-align: center;
  margin-bottom: 40px;
  
  .status-icon {
    width: 60px;
    height: 60px;
    background: #ecfdf5;
    color: #10b981;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    font-size: 30px;
  }
  
  h2 { margin: 0 0 8px; color: #111827; }
  p { color: #6b7280; font-size: 14px; margin: 0; }
  .countdown { color: #ef4444; font-weight: bold; }
  
  .amount {
    margin-top: 20px;
    color: #ef4444;
    font-weight: bold;
    .symbol { font-size: 20px; }
    .num { font-size: 36px; }
  }
}

.order-info {
  background: #f9fafb;
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 30px;
  
  .row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
    &:last-child { margin-bottom: 0; }
    
    .label { color: #6b7280; }
    .val { color: #374151; font-weight: 500; }
  }
}

.payment-method {
  margin-bottom: 30px;
  .method-title { font-weight: 600; margin-bottom: 12px; }
  
  .methods {
    display: flex;
    gap: 20px;
    
    .method-item {
      flex: 1;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      position: relative;
      transition: all 0.2s;
      
      &:hover { border-color: #10b981; }
      &.active { border-color: #10b981; background: #ecfdf5; }
      
      img { width: 32px; height: 32px; }
      span { font-weight: 500; }
      
      .check {
        position: absolute;
        right: 12px;
        color: #10b981;
        font-weight: bold;
      }
    }
  }
}

.qr-section {
  text-align: center;
  margin-bottom: 40px;
  
  .qr-box {
    display: inline-block;
    padding: 20px;
    border: 1px solid #f3f4f6;
    border-radius: 12px;
    
    .qr-placeholder {
      width: 180px;
      height: 180px;
      margin: 0 auto 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      
      &.wechat { background: #22c55e; }
      &.alipay { background: #3b82f6; }
    }
    
    p { margin: 0; font-size: 14px; color: #6b7280; }
  }
}

.actions {
  text-align: center;
  .el-button { min-width: 140px; }
}
</style>