<template>
  <div class="cart-page">
    <div class="container">
      <div class="page-header">
        <h2>购物车 <span class="count" v-if="cartList.length">({{ cartList.length }})</span></h2>
      </div>

      <div class="cart-content" v-if="cartList.length > 0">
        <div class="cart-header">
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

        <el-checkbox-group v-model="selectionSet" class="cart-items-wrapper">
          <div 
            class="cart-item" 
            v-for="item in cartList" 
            :key="item.id"
            :class="{ 'is-selected': selectionSet.includes(item.id) }"
          >
            <div class="col-check item-check">
              <el-checkbox :label="item.id">&nbsp;</el-checkbox>
            </div>
            
            <div class="col-info item-info" @click="navigateToProduct(item.productId)">
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
              <div class="detail">
                <div class="title" :title="item.productTitle">{{ item.productTitle || '商品名称缺失' }}</div>
              </div>
            </div>

            <div class="col-price item-price">
              <span class="price-text">¥{{ formatPrice(item.price) }}</span>
            </div>
            <div class="col-qty item-qty">
              <el-input-number 
                v-model="item.quantity" 
                :min="1" 
                :max="99"
                size="small"
                @change="(val: number) => onQuantityChange(item, val)"
              />
            </div>

            <div class="col-total item-total">
              <span class="total-text">¥{{ calculateItemTotal(item) }}</span>
            </div>

            <div class="col-action item-action">
              <el-popconfirm 
                title="确定移除该商品吗？"
                confirm-button-text="删除"
                cancel-button-text="取消"
                width="200"
                @confirm="handleDelete([item.id])"
              >
                <template #reference>
                  <el-button link type="danger" class="delete-btn">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </el-checkbox-group>
      </div>

      <el-empty v-else description="购物车空空如也，快去选购吧">
        <el-button type="primary" round @click="$router.push('/')">去逛逛</el-button>
      </el-empty>
    </div>

    <div class="bottom-bar-placeholder" v-if="cartList.length > 0"></div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue' // 需要确保安装了 icons

// API & Utils (假设路径)
import { getCartList, updateCartQuantity, deleteCartItems } from '@/api/cart'
import type { CartListDto } from '@/dto/cart'
import { getFullImageUrl } from '@/utils/image'

const router = useRouter()

// --- State ---
const cartList = ref<CartListDto[]>([])
const selectionSet = ref<number[]>([])
const loading = ref(false)

// --- Computed ---

// 解决浮点数精度问题的加法器
const totalPrice = computed(() => {
  const sum = selectionSet.value.reduce((acc, id) => {
    const item = cartList.value.find(i => i.id === id)
    if (item) {
      // 避免 19.9 * 3 = 59.699999999 情况，先乘 100 转整数再计算
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

// --- Methods ---

// 格式化价格
const formatPrice = (price: number | string) => {
  return Number(price).toFixed(2)
}

// 计算单行小计 (避免模版中做复杂运算)
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

// 防抖计时器 Map
const debounceTimers = new Map<number, number>()

const onQuantityChange = (item: CartListDto, val: number) => {
  if (!item) return
  
  // 1. 立即更新 UI (保持响应速度)
  item.quantity = val
  
  // 2. 清除旧计时器
  if (debounceTimers.has(item.id)) {
    clearTimeout(debounceTimers.get(item.id))
  }

  // 3. 设置新计时器 (500ms 防抖)
  const timerId = window.setTimeout(async () => {
    try {
      await updateCartQuantity({ id: item.id, quantity: val })
      debounceTimers.delete(item.id)
    } catch (e) {
      console.error(e)
      ElMessage.warning('数量同步失败，请刷新页面')
      loadData() // 失败回滚
    }
  }, 500)
  
  debounceTimers.set(item.id, timerId)
}

const handleDelete = async (ids: number[]) => {
  if (!ids.length) return
  try {
    await deleteCartItems(ids)
    ElMessage.success('已删除')
    
    // 本地移除
    cartList.value = cartList.value.filter(item => !ids.includes(item.id))
    selectionSet.value = selectionSet.value.filter(id => !ids.includes(id))
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// 批量删除 (保留 MessageBox 强提醒)
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
  if (!selectionSet.value.length) return ElMessage.warning('请选择商品')
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
// 变量定义
$primary-color: #10b981;
$danger-color: #ef4444;
$text-main: #1f2937;
$text-sub: #6b7280;
$border-color: #f3f4f6;
$bg-hover: #f9fafb;

.cart-page {
  background-color: #f3f4f6;
  min-height: calc(100vh - 64px);
  padding: 32px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-header {
  margin-bottom: 20px;
  h2 {
    font-size: 22px;
    color: $text-main;
    font-weight: 600;
    display: flex;
    align-items: baseline;
    .count {
      margin-left: 8px;
      font-size: 14px;
      color: $text-sub;
      font-weight: normal;
    }
  }
}

// 表格通用列宽布局
.cart-header, .cart-item {
  display: flex;
  align-items: center;
}
.col-check { width: 50px; padding-left: 12px; }
.col-info { flex: 1; min-width: 300px; padding: 0 16px; }
.col-price { width: 140px; text-align: center; }
.col-qty { width: 140px; text-align: center; }
.col-total { width: 140px; text-align: center; }
.col-action { width: 100px; text-align: center; }

// 表头样式
.cart-header {
  background: #fff;
  padding: 16px 24px;
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid $border-color;
  font-size: 14px;
  color: $text-sub;
  font-weight: 500;
}

.cart-items-wrapper {
  background: #fff;
  border-radius: 0 0 8px 8px;
  min-height: 200px;
}

// 商品行样式
.cart-item {
  padding: 24px;
  border-bottom: 1px solid $border-color;
  transition: all 0.25s ease;
  
  &:last-child { border-bottom: none; }
  
  &:hover {
    background-color: $bg-hover;
  }
  
  &.is-selected {
    background-color: #f0fdf4; // 选中后淡淡的绿色背景
  }

  .item-info {
    display: flex;
    cursor: pointer;
    
    .thumb {
      width: 88px;
      height: 88px;
      border-radius: 6px;
      border: 1px solid #e5e7eb;
      flex-shrink: 0;
      background: #f9fafb;
      
      .image-slot {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100%;
        color: #d1d5db;
        font-size: 24px;
      }
    }
    
    .detail {
      margin-left: 16px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      
      .title {
        font-size: 15px;
        color: $text-main;
        line-height: 1.4;
        margin-bottom: 6px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        
        &:hover { color: $primary-color; }
      }
      
      .sku-props {
        font-size: 12px;
        color: #9ca3af;
        background: #f3f4f6;
        padding: 2px 6px;
        border-radius: 4px;
        align-self: flex-start;
      }
    }
  }

  .item-price {
    .price-text {
      font-family: Arial, sans-serif;
      color: #4b5563;
      font-weight: 500;
      font-size: 14px;
    }
  }

  .item-total {
    .total-text {
      font-family: Arial, sans-serif;
      color: $danger-color;
      font-weight: bold;
      font-size: 15px;
    }
  }
  
  .delete-btn {
    font-size: 13px;
    &:hover { font-weight: bold; }
  }
}

// 底部结算栏占位与实体
.bottom-bar-placeholder { height: 72px; }
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 -4px 16px rgba(0,0,0,0.06);
  border-top: 1px solid #e5e7eb;
  
  .bar-container {
    max-width: 1200px;
    margin: 0 auto;
    height: 72px;
    padding: 0 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .left-action {
      display: flex;
      align-items: center;
      gap: 24px;
    }
    
    .right-action {
      display: flex;
      align-items: center;
      gap: 32px;
      
      .total-info {
        display: flex;
        align-items: baseline;
        
        .text-gray { font-size: 14px; color: #6b7280; margin-right: 16px; }
        .highlight-num { color: $primary-color; margin: 0 2px; }
        
        .total-label { font-size: 14px; color: #1f2937; }
        .total-price {
          font-size: 26px;
          color: $danger-color;
          font-weight: bold;
          font-family: Arial, sans-serif;
          margin-left: 4px;
          
          small { font-size: 16px; margin-right: 2px; }
        }
      }
      
      .checkout-btn {
        width: 140px;
        height: 44px;
        font-size: 16px;
        font-weight: 600;
        border-radius: 22px; // 圆角按钮更现代
        box-shadow: 0 4px 10px rgba(16, 185, 129, 0.2);
        transition: transform 0.1s;
        
        &:active { transform: scale(0.98); }
        &:disabled { box-shadow: none; }
      }
    }
  }
}
</style>