<template>
  <div class="cart-page">
    <div class="container">
      <div class="page-header">
        <h2>购物车 <span class="count" v-if="cartList.length">({{ cartList.length }})</span></h2>
      </div>

      <div class="cart-list" v-if="cartList.length > 0">
        <div class="cart-header">
          <el-checkbox 
            v-model="isAllChecked" 
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
          >
            全选
          </el-checkbox>
          <span class="col-info">商品信息</span>
          <span class="col-price">单价</span>
          <span class="col-qty">数量</span>
          <span class="col-total">小计</span>
          <span class="col-action">操作</span>
        </div>

        <div class="cart-item" v-for="item in cartList" :key="item.id">
          <div class="item-check">
            <el-checkbox v-model="selectionSet" :label="item.id" @change="handleSelectionChange">
              &nbsp; </el-checkbox>
          </div>
          
          <div class="item-info" @click="$router.push(`/product/${item.productId}`)">
            <el-image :src="item.image" class="thumb" fit="cover" />
            <div class="detail">
              <div class="title">{{ item.title }}</div>
              <div class="seller">卖家：{{ item.sellerName }}</div>
            </div>
          </div>

          <div class="item-price">¥ {{ item.price }}</div>

          <div class="item-qty">
            <el-input-number 
              v-model="item.quantity" 
              :min="1" 
              :max="item.stock || 1" 
              size="small"
              @change="(val) => handleQuantityChange(item.id, val)" 
            />
          </div>

          <div class="item-total">¥ {{ (item.price * item.quantity).toFixed(2) }}</div>

          <div class="item-action">
            <el-button link type="danger" @click="handleDelete([item.id])">删除</el-button>
          </div>
        </div>
      </div>

      <el-empty v-else description="购物车空空如也">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </el-empty>
    </div>

    <div class="bottom-bar" v-if="cartList.length > 0">
      <div class="bar-container">
        <div class="left">
          <el-checkbox 
            v-model="isAllChecked" 
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
          >
            全选
          </el-checkbox>
          <el-button link @click="handleBatchDelete" :disabled="selectionSet.length === 0">
            删除选中
          </el-button>
        </div>
        <div class="right">
          <div class="total-info">
            <span>已选 {{ selectionSet.length }} 件，</span>
            <span>合计：</span>
            <span class="price">¥ {{ totalPrice }}</span>
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
import { getCartList, updateCartItem, deleteCartItems } from '@/api/cart'
import type { CartItemDto } from '@/dto/cart'

const router = useRouter()
const cartList = ref<CartItemDto[]>([])
const selectionSet = ref<number[]>([]) // 存储选中的购物车ID

// 计算属性：总价
const totalPrice = computed(() => {
  let sum = 0
  selectionSet.value.forEach(id => {
    const item = cartList.value.find(i => i.id === id)
    if (item) {
      sum += item.price * item.quantity
    }
  })
  return sum.toFixed(2)
})

// 全选状态逻辑
const isAllChecked = computed({
  get: () => cartList.value.length > 0 && selectionSet.value.length === cartList.value.length,
  set: (val) => { /* logic handled in handleCheckAllChange */ }
})

const isIndeterminate = computed(() => {
  return selectionSet.value.length > 0 && selectionSet.value.length < cartList.value.length
})

// 加载数据
const loadData = async () => {
  try {
    const res = await getCartList()
    // 假设后端返回的数据符合 CartItemDto
    cartList.value = res || []
  } catch (e) {
    console.error(e)
    // Mock 数据用于展示效果（因为还没写后端）
    if (cartList.value.length === 0) {
      cartList.value = [
        { id: 101, productId: 1, title: 'iPhone 13 Pro 演示机', image: 'https://img14.360buyimg.com/n0/jfs/t1/202157/16/16309/86720/6178e246E9656839d/0861110825704a2c.jpg', price: 4500, quantity: 1, stock: 1, sellerName: '数码发烧友' },
        { id: 102, productId: 3, title: 'Java编程思想', image: 'https://img14.360buyimg.com/n0/jfs/t1/1867/26/11488/146050/5bd04183E00388701/374528c3194073f1.jpg', price: 45, quantity: 2, stock: 5, sellerName: '学霸小明' }
      ]
    }
  }
}

// 事件处理
const handleCheckAllChange = (val: boolean) => {
  selectionSet.value = val ? cartList.value.map(item => item.id) : []
}

const handleSelectionChange = (val: any) => {
  // 自动触发 computed 更新
}

const handleQuantityChange = async (id: number, val: number | undefined) => {
  if (!val) return
  try {
    await updateCartItem(id, val)
  } catch (e) { console.error(e) }
}

const handleDelete = async (ids: number[]) => {
  try {
    await ElMessageBox.confirm('确定将选中商品移出购物车吗？', '提示', { type: 'warning' })
    await deleteCartItems(ids)
    ElMessage.success('删除成功')
    // 本地移除
    cartList.value = cartList.value.filter(item => !ids.includes(item.id))
    selectionSet.value = selectionSet.value.filter(id => !ids.includes(id))
  } catch (e) { /* cancel */ }
}

const handleBatchDelete = () => {
  handleDelete(selectionSet.value)
}

const handleCheckout = () => {
  // 购物车结算，跳转到 OrderCreate
  // 由于我们 OrderCreate 目前只支持单商品购买 (productId)，需要改造 OrderCreate 支持购物车结算
  // 这里暂时传递一个标记，或者将选中的 cartIds 传过去
  
  const selectedCartIds = selectionSet.value.join(',')
  router.push({ 
    path: '/order/create', 
    query: { cartIds: selectedCartIds } 
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.cart-page {
  background-color: #f9fafb;
  min-height: calc(100vh - 64px);
  padding: 40px 20px 100px; /* 底部留出空间给结算栏 */
}

.container {
  max-width: 1100px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  h2 { margin: 0; font-size: 24px; color: #111827; }
  .count { font-size: 18px; color: #6b7280; font-weight: normal; }
}

.cart-list {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  overflow: hidden;
}

.cart-header {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background: #f3f4f6;
  font-size: 14px;
  color: #6b7280;
  
  .col-info { flex: 1; margin-left: 20px; }
  .col-price { width: 120px; text-align: center; }
  .col-qty { width: 150px; text-align: center; }
  .col-total { width: 120px; text-align: center; }
  .col-action { width: 80px; text-align: center; }
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f3f4f6;
  transition: background 0.2s;
  
  &:last-child { border-bottom: none; }
  &:hover { background: #f9fafb; }

  .item-info {
    flex: 1;
    display: flex;
    gap: 16px;
    margin-left: 10px;
    cursor: pointer;
    
    .thumb { width: 80px; height: 80px; border-radius: 6px; border: 1px solid #e5e7eb; }
    .detail {
      display: flex;
      flex-direction: column;
      justify-content: center;
      .title { font-weight: 500; margin-bottom: 6px; color: #111827; }
      .seller { font-size: 12px; color: #9ca3af; }
    }
  }
  
  .item-price { width: 120px; text-align: center; color: #6b7280; }
  .item-qty { width: 150px; text-align: center; }
  .item-total { width: 120px; text-align: center; color: #ef4444; font-weight: bold; }
  .item-action { width: 80px; text-align: center; }
}

/* 底部结算栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  box-shadow: 0 -4px 6px -1px rgba(0,0,0,0.05);
  z-index: 99;
  
  .bar-container {
    max-width: 1100px;
    margin: 0 auto;
    height: 72px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    
    .left {
      display: flex;
      align-items: center;
      gap: 20px;
    }
    
    .right {
      display: flex;
      align-items: center;
      gap: 24px;
      
      .total-info {
        font-size: 14px;
        .price { font-size: 24px; color: #ef4444; font-weight: bold; }
      }
      
      .checkout-btn {
        width: 120px;
        background-color: #10b981;
        border-color: #10b981;
        font-weight: 600;
        &:disabled { background-color: #a7f3d0; border-color: #a7f3d0; }
      }
    }
  }
}
</style>