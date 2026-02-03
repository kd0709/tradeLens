<template>
  <div class="cart-page">
    <div class="container">
      <div class="page-header">
        <h2>购物车 <span class="count" v-if="cartList.length">({{ cartList.length }})</span></h2>
      </div>

      <div class="cart-list" v-if="cartList.length > 0">
        <div class="cart-header">
          <el-checkbox 
            :model-value="isAllChecked" 
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

        <el-checkbox-group v-model="selectionSet">
          <div class="cart-item" v-for="(item,index) in cartList" :key="item.id">
            <div class="item-check">
              <el-checkbox :label="item.id" />
            </div>
            
            <div class="item-info" @click="$router.push(`/product/${item.productId}`)">
              <el-image 
                :src="getFullImageUrl(item.productImage)" 
                class="thumb" 
                fit="cover" 
                :lazy="true"
              />
              <div class="detail">
                <div class="title">{{ item.productTitle || '商品名称缺失' }}</div>
                <div class="seller">卖家：待补充</div>
              </div>
            </div>

            <div class="item-price">¥{{ Number(item.price).toFixed(2) }}</div>


            <div class="item-qty">
              <el-input-number 
                v-model="item.quantity" 
                :min="1" 
                size="small"
                @change="(val: number) => handleQuantityChange(item.id, val)"
              />
            </div>

            <div class="item-total">
              ¥{{ (Number(item.price) * item.quantity).toFixed(2) }}
            </div>

            <div class="item-action">
              <el-button link type="danger" @click="handleDelete([item.id])">删除</el-button>
            </div>
          </div>
        </el-checkbox-group>
      </div>

      <el-empty v-else description="购物车空空如也">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </el-empty>
    </div>

    <div class="bottom-bar" v-if="cartList.length > 0">
      <div class="bar-container">
        <div class="left">
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
            @click="handleBatchDelete" 
            :disabled="selectionSet.length === 0"
          >
            删除选中
          </el-button>
        </div>
        <div class="right">
          <div class="total-info">
            <span>已选 {{ selectionSet.length }} 件，</span>
            <span>合计：</span>
            <span class="price">¥{{ totalPrice }}</span>
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

// API 导入
import { getCartList, updateCartQuantity, deleteCartItems } from '@/api/cart'

// DTO 导入
import type { CartListDto, UpdateCartQuantityDto } from '@/dto/cart'
import { getFullImageUrl } from '@/utils/image'

const router = useRouter()

// 数据列表
const cartList = ref<CartListDto[]>([])
const selectionSet = ref<number[]>([])  
const currentUpdateDto = ref<UpdateCartQuantityDto>()


// 计算总价（只算选中的商品）
const totalPrice = computed(() => {
  return selectionSet.value
    .reduce((sum, id) => {
      const item = cartList.value.find(i => i.id === id)
      return item ? sum + item.price * item.quantity : sum
    }, 0)
    .toFixed(2)
})

// 全选 / 半选 状态
const isAllChecked = computed({
  get: () => cartList.value.length > 0 && selectionSet.value.length === cartList.value.length,
  set: () => {}  // set 交给 handleCheckAllChange 处理
})

const isIndeterminate = computed(() => 
  selectionSet.value.length > 0 && 
  selectionSet.value.length < cartList.value.length
)

// 加载购物车列表
const loadData = async () => {
  try {
    const res = await getCartList()
    cartList.value = res || []
    console.log(cartList.value)
  } catch (e) {
    console.error('加载购物车失败:', e)
    ElMessage.error('加载购物车失败，请稍后重试')
    cartList.value = []
  }
}

// 全选/取消全选
const handleCheckAllChange = (checked: boolean) => {
  selectionSet.value = checked 
    ? cartList.value.map(item => item.id) 
    : []
}

// 修改商品数量
const handleQuantityChange = async (id: number, newVal: number) => {
  const item = cartList.value.find(i => i.id === id)
  if (!item) return
  try {
    currentUpdateDto.value = { id, quantity: newVal }
    await updateCartQuantity(currentUpdateDto.value)
    // 成功后更新本地
    item.quantity = newVal
  } catch (e) {
    console.error('更新数量失败:', e)
    ElMessage.error('修改数量失败')
  }
}

// 删除（支持单个或批量）
const handleDelete = async (ids: number[]) => {
  if (!ids.length) return

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${ids.length} 件商品吗？`,
      '提示',
      { type: 'warning' }
    )
    
    await deleteCartItems(ids)
    ElMessage.success('删除成功')

    // 更新本地列表
    cartList.value = cartList.value.filter(item => !ids.includes(item.id))
    selectionSet.value = selectionSet.value.filter(id => !ids.includes(id))
  } catch (err) {
    // 用户取消不提示
    if (err !== 'cancel') {
      ElMessage.error('删除失败，请重试')
    }
  }
}

const handleBatchDelete = () => {
  handleDelete([...selectionSet.value])
}

// 去结算
const handleCheckout = () => {
  if (!selectionSet.value.length) {
    ElMessage.warning('请至少选择一件商品')
    return
  }

  const cartIds = selectionSet.value.join(',')
  router.push({
    path: '/order/create',
    query: { cartIds }
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