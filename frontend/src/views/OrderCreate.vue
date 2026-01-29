<template>
  <div class="order-create-page">
    <div class="container" v-loading="loading">
      
      <div class="page-header">
        <el-page-header @back="router.back()" content="确认订单" />
      </div>

      <div class="section-card address-section">
        <div class="card-title">
          <span>收货地址</span>
          <el-button link type="primary" @click="addressDialogVisible = true">
            <el-icon><Plus /></el-icon> 新增地址
          </el-button>
        </div>
        
        <div class="address-grid">
          <div 
            v-for="addr in addressList" 
            :key="addr.id"
            :class="['address-item', { active: selectedAddressId === addr.id }]"
            @click="selectedAddressId = addr.id"
          >
            <div class="name-row">
              <span class="name">{{ addr.receiverName }}</span>
              <span class="phone">{{ addr.receiverPhone }}</span>
              <el-tag v-if="addr.isDefault" size="small" type="success" effect="plain">默认</el-tag>
            </div>
            <div class="detail-row">
              {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}
            </div>
            <div class="check-icon" v-if="selectedAddressId === addr.id">
              <el-icon><Select /></el-icon>
            </div>
          </div>
          
          <div v-if="addressList.length === 0" class="empty-address" @click="addressDialogVisible = true">
            <el-icon class="icon"><LocationInformation /></el-icon>
            <p>暂无收货地址，点击添加</p>
          </div>
        </div>
      </div>

      <div class="section-card product-section" v-if="product">
        <div class="card-title">商品信息</div>
        <div class="product-row">
          <el-image :src="product.images[0]" class="thumb" fit="cover" />
          <div class="info">
            <div class="title">{{ product.title }}</div>
            <div class="tags">
              <el-tag size="small" type="info">{{ getConditionText(product.conditionLevel) }}</el-tag>
            </div>
            <div class="price-line">
              <span class="label">单价：</span>
              <span class="price">¥ {{ product.price }}</span>
            </div>
          </div>
        </div>
        <div class="calc-row">
          <div class="item">
            <span>配送方式</span>
            <span>快递 免邮</span>
          </div>
          <div class="item">
            <span>购买数量</span>
            <span>x 1</span>
          </div>
        </div>
      </div>

    </div>

    <div class="bottom-bar">
      <div class="bar-container">
        <div class="total-box">
          <span>实付款：</span>
          <span class="price">¥ {{ product?.price || 0 }}</span>
        </div>
        <el-button 
          type="primary" 
          size="large" 
          class="submit-btn" 
          :loading="submitting"
          @click="handleSubmit"
        >
          提交订单
        </el-button>
      </div>
    </div>

    <el-dialog v-model="addressDialogVisible" title="新增收货地址" width="500px">
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人">
          <el-input v-model="addressForm.receiverName" placeholder="名字" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="addressForm.receiverPhone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="区域">
          <el-row :gutter="10">
            <el-col :span="8"><el-input v-model="addressForm.province" placeholder="省" /></el-col>
            <el-col :span="8"><el-input v-model="addressForm.city" placeholder="市" /></el-col>
            <el-col :span="8"><el-input v-model="addressForm.district" placeholder="区" /></el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址">
           <el-input v-model="addressForm.detailAddress" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus, Select, LocationInformation } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '@/api/product'
import { getAddressList, addAddress } from '@/api/address'
import { createOrder } from '@/api/order'
import type { ProductDto } from '@/dto/product'
import type { AddressDto } from '@/dto/address'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const product = ref<ProductDto | null>(null)
const addressList = ref<AddressDto[]>([])
const selectedAddressId = ref<number | undefined>(undefined)

// 地址弹窗
const addressDialogVisible = ref(false)
const addressForm = reactive<AddressDto>({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0
})

const getConditionText = (level: number) => {
  const map: Record<number, string> = { 1: '全新', 2: '99新', 3: '9成新', 4: '8成新' }
  return map[level] || '二手'
}

// 初始化加载
const init = async () => {
  const productId = Number(route.query.productId)
  if (!productId) {
    ElMessage.error('参数错误')
    router.back()
    return
  }

  loading.value = true
  try {
    // 1. 获取商品信息
    product.value = await getProductDetail(productId)
    
    // 2. 获取地址列表
    await loadAddresses()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadAddresses = async () => {
  addressList.value = await getAddressList()
  // 自动选中默认地址，或者第一个地址
  if (!selectedAddressId.value && addressList.value.length > 0) {
    const defaultAddr = addressList.value.find(a => a.isDefault === 1)
    selectedAddressId.value = defaultAddr ? defaultAddr.id : addressList.value[0].id
  }
}

// 保存地址
const handleSaveAddress = async () => {
  if (!addressForm.receiverName || !addressForm.receiverPhone) {
    return ElMessage.warning('请填写完整信息')
  }
  try {
    await addAddress({ ...addressForm })
    ElMessage.success('地址添加成功')
    addressDialogVisible.value = false
    // 重新加载并选中新地址
    await loadAddresses()
    // 简单处理：选中最新一个（假设后端按时间倒序）
    // 严谨做法是后端返回新ID
  } catch (e) { console.error(e) }
}

// 提交订单
const handleSubmit = async () => {
  if (!selectedAddressId.value) return ElMessage.warning('请选择收货地址')
  if (!product.value) return

  submitting.value = true
  try {
    const orderId = await createOrder({
      productId: product.value.id,
      quantity: 1, // 默认为1
      addressId: selectedAddressId.value
    })
    
    ElMessage.success('下单成功！')
    // 跳转到用户中心的“我买到的”页面，让用户去支付
    // 或者直接跳到 /user?tab=buying
    router.replace('/user')
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  init()
})
</script>

<style scoped lang="scss">
.order-create-page {
  background-color: #f9fafb;
  min-height: 100vh;
  padding-bottom: 80px; /* 为底部栏留空 */
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);

  .card-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

/* 地址网格 */
.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;

  .address-item {
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 16px;
    cursor: pointer;
    position: relative;
    transition: all 0.2s;
    
    &:hover { border-color: #10b981; }
    &.active {
      border-color: #10b981;
      background-color: #ecfdf5;
      box-shadow: 0 0 0 1px #10b981;
    }

    .name-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      font-weight: 500;
      .phone { color: #6b7280; font-size: 13px; }
    }
    
    .detail-row {
      font-size: 13px;
      color: #4b5563;
      line-height: 1.4;
    }

    .check-icon {
      position: absolute;
      right: 0;
      bottom: 0;
      background: #10b981;
      color: #fff;
      padding: 2px 6px;
      border-top-left-radius: 8px;
      border-bottom-right-radius: 6px; /* 微调圆角 */
    }
  }

  .empty-address {
    border: 1px dashed #d1d5db;
    border-radius: 8px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #9ca3af;
    cursor: pointer;
    min-height: 100px;
    
    &:hover { border-color: #10b981; color: #10b981; }
    .icon { font-size: 24px; margin-bottom: 8px; }
  }
}

/* 商品信息 */
.product-row {
  display: flex;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
  margin-bottom: 16px;

  .thumb {
    width: 80px;
    height: 80px;
    border-radius: 6px;
    background: #f9fafb;
  }
  
  .info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .title { font-size: 15px; font-weight: 500; color: #111827; }
    .price-line {
      font-size: 14px;
      .price { color: #ef4444; font-weight: bold; }
    }
  }
}

.calc-row {
  .item {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    color: #4b5563;
    margin-bottom: 8px;
    
    &:last-child { margin-bottom: 0; }
  }
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  padding: 12px 20px;
  z-index: 99;
  
  .bar-container {
    max-width: 800px;
    margin: 0 auto;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 20px;
    
    .total-box {
      font-size: 14px;
      .price {
        font-size: 24px;
        color: #ef4444;
        font-weight: bold;
      }
    }

    .submit-btn {
      width: 140px;
      font-weight: 600;
      background-color: #10b981;
      border-color: #10b981;
      
      &:hover { background-color: #059669; }
    }
  }
}
</style>