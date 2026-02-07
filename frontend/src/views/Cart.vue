<template>
  <div class="cart-page">
    <div class="bg-decoration left"></div>
    <div class="bg-decoration right"></div>

    <div class="container">
      <Transition name="fade-down" appear>
        <div class="page-header">
          <h2>
            购物车 
            <span class="count" v-if="cartList.length">({{ cartList.length }})</span>
          </h2>
          <p class="subtitle">管理你的心仪之选</p>
        </div>
      </Transition>

      <Transition name="fade-up" appear>
        <div class="cart-card" v-if="cartList.length > 0">
          
          <div class="cart-header grid-layout">
            <div class="col-check">
              <el-checkbox 
                :model-value="isAllChecked" 
                :indeterminate="isIndeterminate"
                @change="handleCheckAllChange"
              >
                全选
              </el-checkbox>
            </div>
            <div class="col-info">商品信息</div>
            <div class="col-price">单价</div>
            <div class="col-qty">数量</div>
            <div class="col-total">小计</div>
            <div class="col-action">操作</div>
          </div>

          <div class="cart-list">
            <el-checkbox-group v-model="selectionSet">
              <TransitionGroup name="list-anim">
                <div 
                  class="cart-item grid-layout" 
                  v-for="item in cartList" 
                  :key="item.id"
                  :class="{ 'is-selected': selectionSet.includes(item.id) }"
                >
                  <div class="col-check center">
                    <el-checkbox :label="item.id" class="custom-checkbox">&nbsp;</el-checkbox>
                  </div>
                  
                  <div class="col-info" @click="navigateToProduct(item.productId)">
                    <div class="thumb-wrapper">
                      <el-image 
                        :src="getFullImageUrl(item.productImage)" 
                        class="thumb" 
                        fit="cover" 
                        lazy
                      >
                        <template #error>
                          <div class="image-slot">
                            <el-icon><Picture /></el-icon>
                          </div>
                        </template>
                      </el-image>
                    </div>
                    <div class="detail">
                      <div class="title" :title="item.productTitle">
                        {{ item.productTitle || '商品名称缺失' }}
                      </div>
                    </div>
                  </div>

                  <div class="col-price center">
                    <span class="price-text">¥{{ formatPrice(item.price) }}</span>
                  </div>

                  <div class="col-qty center">
                    <el-input-number 
                      v-model="item.quantity" 
                      :min="1" 
                      :max="99"
                      size="small"
                      @change="(val: number) => onQuantityChange(item, val)"
                      class="custom-input-number"
                    />
                  </div>

                  <div class="col-total center">
                    <span class="total-text">¥{{ calculateItemTotal(item) }}</span>
                  </div>

                  <div class="col-action center">
                    <el-popconfirm 
                      title="确定要移除吗？"
                      confirm-button-text="移除"
                      cancel-button-text="暂不"
                      width="200"
                      @confirm="handleDelete([item.id])"
                    >
                      <template #reference>
                        <el-button link type="danger" :icon="Delete" circle class="delete-btn" />
                      </template>
                    </el-popconfirm>
                  </div>
                </div>
              </TransitionGroup>
            </el-checkbox-group>
          </div>
        </div>

        <div v-else class="empty-card">
           <el-empty description="购物车空空如也，快去选购吧">
            <el-button type="primary" size="large" round @click="$router.push('/')">去首页逛逛</el-button>
          </el-empty>
        </div>
      </Transition>
    </div>

    <div class="bottom-bar-placeholder" v-if="cartList.length > 0"></div>
    <Transition name="slide-up">
      <div class="bottom-bar" v-if="cartList.length > 0">
        <div class="bar-container">
          <div class="left-action">
            <el-checkbox 
              :model-value="isAllChecked" 
              :indeterminate="isIndeterminate"
              @change="handleCheckAllChange"
            >
              全选
            </el-checkbox>
            <el-button 
              link 
              type="danger" 
              :disabled="selectionSet.length === 0"
              @click="handleBatchDelete"
            >
              删除选中
            </el-button>
          </div>
          
          <div class="right-action">
            <div class="total-info">
              <span class="text-gray">已选 <b class="highlight-num">{{ selectionSet.length }}</b> 件</span>
              <span class="divider"></span>
              <span class="total-label">合计：</span>
              <span class="total-price">
                <small>¥</small>{{ totalPrice }}
              </span>
            </div>
            <el-button 
              type="primary" 
              size="large" 
              class="checkout-btn"
              :disabled="selectionSet.length === 0"
              @click="handleCheckout"
            >
              去结算
            </el-button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
/**
 * 模块化：导入
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Delete } from '@element-plus/icons-vue' // 导入图标

// API & Utils
import { getCartList, updateCartQuantity, deleteCartItems } from '@/api/cart'
import type { CartListDto } from '@/dto/cart'
import { getFullImageUrl } from '@/utils/image'

/**
 * 模块化：状态管理
 */
const router = useRouter()
const cartList = ref<CartListDto[]>([])
const selectionSet = ref<number[]>([])
const loading = ref(false)

/**
 * 模块化：计算属性
 */
// 解决浮点数精度问题的加法器
const totalPrice = computed(() => {
  const sum = selectionSet.value.reduce((acc, id) => {
    const item = cartList.value.find(i => i.id === id)
    if (item) {
      // 避免精度问题，先乘 100 转整数再计算
      return acc + (Number(item.price) * 100 * item.quantity)
    }
    return acc
  }, 0)
  return (sum / 100).toFixed(2)
})

const isAllChecked = computed({
  get: () => cartList.value.length > 0 && selectionSet.value.length === cartList.value.length,
  set: () => {} 
})

const isIndeterminate = computed(() => 
  selectionSet.value.length > 0 && selectionSet.value.length < cartList.value.length
)

/**
 * 模块化：业务逻辑
 */
const formatPrice = (price: number | string) => {
  return Number(price).toFixed(2)
}

const calculateItemTotal = (item: CartListDto) => {
  return ((Number(item.price) * 100 * item.quantity) / 100).toFixed(2)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCartList()
    cartList.value = res || []
  } catch (e) {
    console.error(e)
    ElMessage.error('购物车数据加载失败')
  } finally {
    loading.value = false
  }
}

const handleCheckAllChange = (checked: boolean) => {
  selectionSet.value = checked ? cartList.value.map(item => item.id) : []
}

// 防抖更新数量
const debounceTimers = new Map<number, number>()
const onQuantityChange = (item: CartListDto, val: number) => {
  if (!item) return
  item.quantity = val // 立即响应 UI
  
  if (debounceTimers.has(item.id)) {
    clearTimeout(debounceTimers.get(item.id))
  }

  const timerId = window.setTimeout(async () => {
    try {
      await updateCartQuantity({ id: item.id, quantity: val })
      debounceTimers.delete(item.id)
    } catch (e) {
      console.error(e)
      ElMessage.warning('数量同步失败，请刷新页面')
      loadData()
    }
  }, 500)
  debounceTimers.set(item.id, timerId)
}

const handleDelete = async (ids: number[]) => {
  if (!ids.length) return
  try {
    await deleteCartItems(ids)
    ElMessage.success('已移除商品')
    
    // 本地状态更新，配合 TransitionGroup 产生动画
    cartList.value = cartList.value.filter(item => !ids.includes(item.id))
    selectionSet.value = selectionSet.value.filter(id => !ids.includes(id))
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectionSet.value.length} 件商品吗？`,
      '批量删除',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    handleDelete([...selectionSet.value])
  } catch (e) {
    // Cancelled
  }
}

const handleCheckout = () => {
  if (!selectionSet.value.length) return ElMessage.warning('请至少选择一件商品')
  router.push({
    path: '/order/create',
    query: { cartIds: selectionSet.value.join(',') }
  })
}

const navigateToProduct = (productId: number | string) => {
  router.push(`/product/${productId}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.cart-page {
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  min-height: calc(100vh - 64px);
  padding: 40px 0 80px;
  position: relative;
  overflow-x: hidden; // 防止装饰溢出

  /* 背景装饰 */
  .bg-decoration {
    position: absolute;
    width: 500px;
    height: 500px;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.6;
    z-index: 0;
    
    &.left {
      background: #d1fae5;
      top: -100px;
      left: -150px;
    }
    &.right {
      background: #e0e7ff;
      top: 20%;
      right: -200px;
    }
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.page-header {
  margin-bottom: 24px;
  h2 {
    font-size: 28px;
    color: #111827;
    font-weight: 700;
    margin: 0 0 4px;
    display: flex;
    align-items: center;
    .count {
      margin-left: 8px;
      font-size: 16px;
      color: #9ca3af;
      font-weight: normal;
    }
  }
  .subtitle {
    color: #6b7280;
    font-size: 14px;
  }
}

/* 购物车卡片：毛玻璃效果 */
.cart-card, .empty-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
  border: 1px solid #fff;
  overflow: hidden;
}

.empty-card { padding: 80px 0; }

/* 核心布局：Grid 系统 - 确保严格对齐 */
.grid-layout {
  display: grid;
  /* 列宽定义: Checkbox | Info(弹性) | Price | Qty | Total | Action */
  grid-template-columns: 60px 1fr 140px 160px 140px 100px;
  align-items: center;
}

.cart-header {
  background: rgba(249, 250, 251, 0.8);
  padding: 16px 24px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 14px;
  color: #6b7280;
  font-weight: 600;
  
  // 居中对齐的列
  .col-price, .col-qty, .col-total, .col-action { text-align: center; }
}

.cart-list {
  min-height: 200px;
}

.cart-item {
  padding: 24px;
  border-bottom: 1px solid #f3f4f6;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: transparent;
  
  &:last-child { border-bottom: none; }
  
  &:hover {
    background-color: rgba(240, 253, 244, 0.5); // 极淡的绿色悬浮
  }
  
  &.is-selected {
    background-color: rgba(240, 253, 244, 0.8); // 选中背景
  }

  // 通用居中辅助
  .center {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .col-info {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding-right: 20px;
    
    .thumb-wrapper {
      position: relative;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
      border: 1px solid #e5e7eb;
      width: 80px; height: 80px; flex-shrink: 0;
    }
    .thumb {
      width: 100%; height: 100%;
      .image-slot {
        display: flex; justify-content: center; align-items: center;
        width: 100%; height: 100%; background: #f9fafb; color: #d1d5db; font-size: 20px;
      }
    }
    
    .detail {
      margin-left: 16px;
      flex: 1;
      .title {
        font-size: 15px; color: #1f2937; line-height: 1.5; font-weight: 500;
        margin-bottom: 6px;
        overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
        transition: color 0.2s;
        &:hover { color: #10b981; }
      }
    }
  }

  .price-text { color: #4b5563; font-weight: 500; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
  .total-text { color: #ef4444; font-weight: 700; font-size: 16px; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
  
  .delete-btn {
    font-size: 18px; color: #9ca3af;
    &:hover { color: #ef4444; background: #fee2e2; }
  }
}

/* 底部结算栏 */
.bottom-bar-placeholder { height: 80px; }
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; z-index: 100;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  box-shadow: 0 -4px 20px rgba(0,0,0,0.05);
  border-top: 1px solid rgba(0,0,0,0.05);
  
  .bar-container {
    max-width: 1200px; margin: 0 auto; height: 72px; padding: 0 24px;
    display: flex; justify-content: space-between; align-items: center;
    
    .left-action {
      display: flex; align-items: center; gap: 24px;
    }
    
    .right-action {
      display: flex; align-items: center; gap: 24px;
      
      .total-info {
        display: flex; align-items: baseline; background: #f9fafb; padding: 8px 20px; border-radius: 30px;
        
        .text-gray { font-size: 13px; color: #6b7280; }
        .highlight-num { color: #10b981; font-weight: bold; margin: 0 2px; }
        .divider { width: 1px; height: 12px; background: #d1d5db; margin: 0 16px; }
        
        .total-label { font-size: 14px; color: #374151; margin-right: 4px; }
        .total-price {
          font-size: 24px; color: #ef4444; font-weight: 800; line-height: 1; letter-spacing: -0.5px;
          small { font-size: 14px; font-weight: 600; margin-right: 1px; }
        }
      }
      
      .checkout-btn {
        width: 140px; height: 44px; font-size: 16px; font-weight: 600; border-radius: 22px;
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        border: none;
        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
        &:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4); }
        &:active { transform: translateY(0); }
        &:disabled { background: #d1d5db; box-shadow: none; transform: none; }
      }
    }
  }
}

/* 动画效果 */
.list-anim-move,
.list-anim-enter-active,
.list-anim-leave-active {
  transition: all 0.4s ease;
}
.list-anim-enter-from,
.list-anim-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
.list-anim-leave-active {
  position: absolute;
  width: 100%; // 确保删除时其他元素平滑移动
}

.fade-down-enter-active, .fade-up-enter-active, .slide-up-enter-active {
  transition: all 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.fade-down-enter-from { opacity: 0; transform: translateY(-20px); }
.fade-up-enter-from { opacity: 0; transform: translateY(30px); }
.slide-up-enter-from { transform: translateY(100%); }
</style>