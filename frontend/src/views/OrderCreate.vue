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

      <div class="section-card product-section" v-if="productList.length > 0">
        <div class="card-title">商品清单 ({{ productList.length }})</div>
        
        <div v-for="(item, index) in productList" :key="index" class="product-item-wrapper">
          <div class="product-row">
            <el-image :src="item.image" class="thumb" fit="cover" />
            <div class="info">
              <div class="title">{{ item.title }}</div>
              <div class="tags">
                <el-tag size="small" type="info" v-if="item.conditionLevel">{{ getConditionText(item.conditionLevel) }}</el-tag>
                <el-tag size="small" type="warning" v-else>购物车商品</el-tag>
              </div>
              <div class="price-line">
                <span class="label">单价：</span>
                <span class="price">¥ {{ item.price }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="calc-row">
          <div class="item">
            <span>配送方式</span>
            <span>快递 免邮</span>
          </div>
          <div class="item">
            <span>总数量</span>
            <span>x {{ productList.length }}</span>
          </div>
        </div>
      </div>

    </div>

    <div class="bottom-bar">
      <div class="bar-container">
        <div class="total-box">
          <span>共 {{ productList.length }} 件，实付款：</span>
          <span class="price">¥ {{ totalPrice }}</span>
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
import { getCartList } from '@/api/cart' // 引入购物车接口
import type { AddressDto } from '@/dto/address'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
// 将 product 变量改为 productList 数组
const productList = ref<any[]>([]) 
const addressList = ref<AddressDto[]>([])
const selectedAddressId = ref<number | undefined>(undefined)
const isCartMode = ref(false) // 标记是否为购物车结算

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

// 计算总价
const totalPrice = computed(() => {
  return productList.value.reduce((sum, item) => sum + Number(item.price), 0).toFixed(2)
})

const init = async () => {
  loading.value = true
  try {
    // 1. 获取地址列表
    await loadAddresses()

    // 2. 判断来源：直接购买(productId) 还是 购物车(cartIds)
    const productId = route.query.productId
    const cartIds = route.query.cartIds

    if (productId) {
      // --- 单商品模式 ---
      isCartMode.value = false
      const res = await getProductDetail(Number(productId))
      // 统一转化为数组格式方便渲染
      productList.value = [{
        id: res.id, // 这里是 productId
        title: res.title,
        price: res.price,
        image: res.images?.[0] || '',
        conditionLevel: res.conditionLevel
      }]
    } else if (cartIds) {
      // --- 购物车模式 ---
      isCartMode.value = true
      const idsStr = String(cartIds).split(',')
      const targetIds = idsStr.map(Number)
      
      // 由于没有专门的 getCartByIds 接口，我们先拉取所有购物车，在前端过滤
      // (实际项目中应由后端提供 /api/cart/check-out 接口)
      const allCartItems = await getCartList() || []
      
      // 如果 getCartList 返回为空（因为没后端），我们造一些 Mock 数据兜底演示
      if (allCartItems.length === 0) {
        // Mock 数据逻辑，仅供演示
        productList.value = [
           { id: 101, productId: 1, title: 'iPhone 13 Pro (演示)', image: 'https://img14.360buyimg.com/n0/jfs/t1/202157/16/16309/86720/6178e246E9656839d/0861110825704a2c.jpg', price: 4500 },
           { id: 102, productId: 3, title: 'Java编程思想 (演示)', image: 'https://img14.360buyimg.com/n0/jfs/t1/1867/26/11488/146050/5bd04183E00388701/374528c3194073f1.jpg', price: 45 }
        ].filter(item => targetIds.includes(item.id))
      } else {
        // 真实逻辑
        productList.value = allCartItems
          .filter(item => targetIds.includes(item.id))
          .map(item => ({
            id: item.productId, // 注意：下单需要 productId，不是 cartItemId
            title: item.title,
            price: item.price,
            image: item.image,
            conditionLevel: 0 // 购物车列表可能没返回成色，暂略
          }))
      }
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载订单信息失败')
  } finally {
    loading.value = false
  }
}

const loadAddresses = async () => {
  addressList.value = await getAddressList()
  if (!selectedAddressId.value && addressList.value.length > 0) {
    const defaultAddr = addressList.value.find(a => a.isDefault === 1)
    selectedAddressId.value = defaultAddr ? defaultAddr.id : addressList.value[0].id
  }
}

const handleSaveAddress = async () => {
  if (!addressForm.receiverName || !addressForm.receiverPhone) {
    return ElMessage.warning('请填写完整信息')
  }
  try {
    await addAddress({ ...addressForm })
    ElMessage.success('地址添加成功')
    addressDialogVisible.value = false
    await loadAddresses()
  } catch (e) { console.error(e) }
}

const handleSubmit = async () => {
  if (!selectedAddressId.value) return ElMessage.warning('请选择收货地址')
  if (productList.value.length === 0) return

  submitting.value = true
  try {
    // 核心逻辑：循环下单
    // 因为后端 createOrder 接口每次只接收一个 productId
    const promises = productList.value.map(item => {
      return createOrder({
        productId: item.id, // 这里的 id 已经是 productId 了
        quantity: 1,
        addressId: selectedAddressId.value!
      })
    })

    const results = await Promise.all(promises)
    
    // 如果只创建了一个订单，跳转到支付页面；多个订单则跳转到用户中心
    if (results.length === 1 && results[0]?.orderNo) {
      router.replace(`/pay/${results[0].orderNo}`)
    } else {
      ElMessage.success(`成功创建 ${productList.value.length} 个订单！`)
      router.replace('/user')
    }
  } catch (e) {
    console.error(e)
    // ElMessage 由拦截器处理
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
  padding-bottom: 80px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header { margin-bottom: 20px; }

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
    border-bottom: 1px solid #f3f4f6;
    padding-bottom: 12px;
  }
}

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
    .check-icon {
      position: absolute; right: 0; bottom: 0;
      background: #10b981; color: #fff;
      padding: 2px 6px; border-top-left-radius: 8px;
    }
  }
  .empty-address {
    border: 1px dashed #d1d5db; padding: 20px; text-align: center; color: #9ca3af; cursor: pointer;
  }
}

.product-item-wrapper {
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px dashed #f3f4f6;
  &:last-of-type { border-bottom: none; }
}

.product-row {
  display: flex;
  gap: 16px;

  .thumb { width: 80px; height: 80px; border-radius: 6px; background: #f9fafb; }
  .info {
    flex: 1; display: flex; flex-direction: column; justify-content: space-between;
    .title { font-size: 15px; font-weight: 500; }
    .price-line .price { color: #ef4444; font-weight: bold; }
  }
}

.calc-row {
  border-top: 1px solid #f3f4f6;
  padding-top: 16px;
  .item { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 8px; }
}

.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: #fff; border-top: 1px solid #e5e7eb; padding: 12px 20px; z-index: 99;
  .bar-container {
    max-width: 800px; margin: 0 auto; display: flex; justify-content: flex-end; align-items: center; gap: 20px;
    .total-box { font-size: 14px; .price { font-size: 24px; color: #ef4444; font-weight: bold; } }
    .submit-btn { width: 140px; background-color: #10b981; border-color: #10b981; }
  }
}
</style>