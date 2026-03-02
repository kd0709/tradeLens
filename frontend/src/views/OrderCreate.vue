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
              <el-tag v-if="addr.isDefault === 1" size="small" type="success" effect="plain">默认</el-tag>
            </div>
            <div class="detail-row">
              {{ formatAddress(addr) }}
            </div>
            <div class="check-icon" v-if="selectedAddressId === addr.id">
              <el-icon><Select /></el-icon>
            </div>
          </div>

          <div v-if="!addressList.length" class="empty-address" @click="addressDialogVisible = true">
            <el-icon class="icon"><LocationInformation /></el-icon>
            <p>暂无收货地址，点击添加</p>
          </div>
        </div>
      </div>

      <div v-if="orderItems.length" class="section-card product-section">
        <div class="card-title">商品清单 ({{ orderItems.length }})</div>

        <div v-for="item in orderItems" :key="item.productId" class="product-item-wrapper">
          <div class="product-row">
            <el-image
              :src="getFullImageUrl(item.image)"
              class="thumb"
              fit="cover"
              lazy
            />
            <div class="info">
              <div class="title">{{ item.title }}</div>
              <div class="tags">
                <el-tag v-if="item.conditionLevel" size="small" type="info">
                  {{ getConditionText(item.conditionLevel) }}
                </el-tag>
                <el-tag v-else size="small" type="warning">购物车商品</el-tag>
              </div>
              <div class="price-line">
                <span class="label">单价：</span>
                <span class="price">¥{{ Number(item.price).toFixed(2) }}</span>
              </div>
              <div class="quantity-line" v-if="item.quantity > 1">
                <span class="label">数量：</span>
                <span>x {{ item.quantity }}</span>
              </div>
              <div class="stock-info">
                <el-tag :type="item.productQuantity > 5 ? 'success' : item.productQuantity > 0 ? 'warning' : 'danger'">
                  库存: {{ item.productQuantity }}
                </el-tag>
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
            <span>商品总价</span>
            <span>¥{{ totalPrice }}</span>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无商品信息" />

    </div>

    <div class="bottom-bar" v-if="orderItems.length">
      <div class="bar-container">
        <div class="total-box">
          <span>共 {{ orderItems.length }} 件，实付款：</span>
          <span class="price">¥{{ totalPrice }}</span>
        </div>
        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="submitting"
          :disabled="!selectedAddressId || submitting"
          @click="handleSubmit"
        >
          提交订单
        </el-button>
      </div>
    </div>

    <el-dialog
      v-model="addressDialogVisible"
      title="新增收货地址"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        label-width="80px"
        :rules="addressRules"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="区域">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-input v-model="addressForm.province" placeholder="省" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="addressForm.city" placeholder="市" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="addressForm.district" placeholder="区/县" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input
            v-model="addressForm.detailAddress"
            type="textarea"
            :rows="3"
            placeholder="街道、门牌号、小区等详细地址"
          />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { Plus, Select, LocationInformation } from '@element-plus/icons-vue'

import { getProductDetail } from '@/api/product'
import { getMyAddressList, createAddress } from '@/api/address'
import { createOrder } from '@/api/order'
import { getCartList } from '@/api/cart'
import { getFullImageUrl } from '@/utils/image'
import type { AddressCreateDto, AddressDto } from '@/dto/address'
import type { CreateOrderDto } from '@/dto/order' 

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const addressDialogVisible = ref(false)

const addressFormRef = ref<InstanceType<typeof ElForm> | null>(null)
const addressList = ref<AddressDto[]>([])
const selectedAddressId = ref<number | undefined>(undefined)
const orderItems = ref<Array<{
  productId: number
  title: string
  price: number
  image: string
  conditionLevel?: number
  quantity: number
  productQuantity: number 
}>>([])
const addressForm = reactive<Partial<AddressDto>>({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0
})

const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: ['blur', 'change'] }
  ],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}
const totalPrice = computed(() => {
  const sum = orderItems.value.reduce((acc, item) => {
    const qty = item.quantity || 1
    return acc + Number(item.price) * qty
  }, 0)
  return sum.toFixed(2)
})

const getConditionText = (level?: number) => {
  if (!level) return ''
  const map: Record<number, string> = {
    1: '全新',
    2: '几乎全新',
    3: '轻微使用',
    4: '明显使用'
  }
  return map[level] || '未知成色'
}

const formatAddress = (addr: AddressDto) => {
  return [addr.province, addr.city, addr.district, addr.detailAddress]
    .filter(Boolean)
    .join(' ')
}

const loadAddresses = async () => {
  try {
    addressList.value = (await getMyAddressList()) || []
    if (addressList.value.length > 0 && !selectedAddressId.value) {
      const defaultAddr = addressList.value.find(a => a.isDefault === 1)
      selectedAddressId.value = defaultAddr?.id ?? addressList.value[0]?.id
    }
  } catch (err) {
    // 获取地址列表失败已由 ElMessage处理
  }
}

const initData = async () => {
  loading.value = true
  try {
    await loadAddresses()

    const { productId, cartIds } = route.query

    if (productId) {
      const pid = Number(productId)
      if (isNaN(pid)) {
        ElMessage.warning('无效的商品ID')
        return router.back()
      }

      const detail = await getProductDetail(pid)
      orderItems.value = [{
        productId: detail.id,
        title: detail.title,
        price: detail.price,
        image: detail.images?.[0] || '',
        conditionLevel: detail.conditionLevel,
        quantity: 1,
        productQuantity: detail.quantity
      }]
    } else if (cartIds) {
      const ids = String(cartIds)
        .split(',')
        .map(id => Number(id.trim()))
        .filter(id => !isNaN(id))

      if (!ids.length) {
        ElMessage.warning('没有有效的购物车项')
        return router.back()
      }

      const cartItems = await getCartList() || []

      orderItems.value = cartItems
        .filter(item => ids.includes(item.id))
        .map(item => ({
          productId: item.productId,
          title: item.productTitle || '商品名称缺失',
          price: Number(item.price),
          image: item.productImage || '',
          conditionLevel: undefined,
          quantity: Number(item.quantity) || 1,
          productQuantity: item.productQuantity
        }))
    } else {
      ElMessage.warning('缺少必要参数（商品或购物车）')
      router.back()
      return
    }
  } catch (err: any) {
    //订单初始化失败已由 ElMessage处理
    ElMessage.error('加载订单信息失败')
    setTimeout(() => router.back(), 1200)
  } finally {
    loading.value = false
  }
}

const handleSaveAddress = async () => {
  if (!addressFormRef.value) return

  try {
    await addressFormRef.value.validate()
    await createAddress(addressForm as AddressCreateDto)
    ElMessage.success('地址添加成功')
    addressDialogVisible.value = false

    Object.assign(addressForm, {
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: ''
    })

    await loadAddresses()
  } catch (err) {
  }
}

const handleSubmit = async () => {
  // 1. 基础校验
  if (!selectedAddressId.value) {
    return ElMessage.warning('请选择收货地址')
  }
  if (!orderItems.value.length) return
  
  // 1.5. 库存校验
  for (const item of orderItems.value) {
    if (item.quantity > item.productQuantity) {
      return ElMessage.error(`商品《${item.title}》库存不足，当前库存: ${item.productQuantity}，请求购买: ${item.quantity}`)
    }
  }

  submitting.value = true
  
  try {
    // 2. 构造符合后端新 DTO 格式的数据
    // 从路由 query 中获取需要清理的购物车 ID
    const cartIdList = route.query.cartIds 
      ? String(route.query.cartIds).split(',').map(id => Number(id)) 
      : []

    const dto = {
      addressId: selectedAddressId.value,
      // 组装商品列表
      items: orderItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity || 1
      })),
      // 传入购物车 ID，让后端顺便把它们删了
      cartIds: cartIdList
    }

    // 3. 发起【一次性】批量下单请求
    // 注意：这里的 createOrder 接口现在接收的是包含列表的对象，不再是循环调用
    const result = await createOrder(dto)

    // 4. 下单成功处理
    if (result && result.orderNo) {
      ElMessage.success('订单创建成功')
      // 跳转到支付页，传递这个包含多个商品的统一订单号
      router.replace(`/pay/${result.orderNo}`)
    } else {
      // 如果后端返回结构不同，跳转到订单列表页
      router.replace('/user/orders')
    }
    
  } catch (err: any) {
    //订单创建失败已由 ElMessage处理
    // 具体的错误提示（如：商品库存不足、商品不可购买等）
    ElMessage.error(err.response?.data?.message || '创建订单失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(initData)
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
    .stock-info { margin-top: 6px; }
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