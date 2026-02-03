<template>
  <div class="order-create-page">
    <div class="container" v-loading="loading">
      <div class="page-header">
        <el-page-header @back="router.back()" content="确认订单" />
      </div>

      <!-- 收货地址 -->
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

      <!-- 商品清单 -->
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

    <!-- 底部操作栏 -->
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

    <!-- 新增地址弹窗 -->
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

// API 接口
import { getProductDetail } from '@/api/product'
import { getMyAddressList, createAddress } from '@/api/address'
import { createOrder } from '@/api/order'
import { getCartList } from '@/api/cart'
import { getFullImageUrl } from '@/utils/image'

// DTO 导入
import type { AddressCreateDto, AddressDto } from '@/dto/address'
import type { CreateOrderDto } from '@/dto/order' 

const route = useRoute()
const router = useRouter()

// 状态定义
const loading = ref(false)
const submitting = ref(false)
const addressDialogVisible = ref(false)

// 列表数据
const addressFormRef = ref<InstanceType<typeof ElForm> | null>(null)
const addressList = ref<AddressDto[]>([])
const selectedAddressId = ref<number | undefined>(undefined)
const orderItems = ref<Array<{
  productId: number
  title: string
  price: number
  image: string
  conditionLevel?: number
  quantity: number // 购物车可能有，单买默认为1
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

// 地址表单验证规则
const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: ['blur', 'change'] }
  ],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

// 计算总价
const totalPrice = computed(() => {
  const sum = orderItems.value.reduce((acc, item) => {
    const qty = item.quantity || 1
    return acc + Number(item.price) * qty
  }, 0)
  return sum.toFixed(2)
})

// 获取状态文本
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
    console.error('获取地址列表失败', err)
  }
}

const initData = async () => {
  loading.value = true
  try {
    await loadAddresses()

    const { productId, cartIds } = route.query

    if (productId) {
      // 单商品购买
      const pid = Number(productId)
      if (isNaN(pid)) {
        ElMessage.warning('无效的商品ID')
        return router.back()
      }

      const detail = await getProductDetail(pid)
      // 假设返回的是 ProductDto 结构
      orderItems.value = [{
        productId: detail.id,
        title: detail.title,
        price: detail.price,
        image: detail.images?.[0] || '',
        conditionLevel: detail.conditionLevel,
        quantity: 1
      }]
    } else if (cartIds) {
      // 购物车结算
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
          quantity: Number(item.quantity) || 1
        }))
    } else {
      ElMessage.warning('缺少必要参数（商品或购物车）')
      router.back()
      return
    }
  } catch (err: any) {
    console.error('初始化订单数据失败', err)
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

    // 重置表单
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
    // 校验失败或接口失败不额外提示，element-plus 会显示
  }
}

const handleSubmit = async () => {
  if (!selectedAddressId.value) {
    return ElMessage.warning('请选择收货地址')
  }
  if (!orderItems.value.length) return

  submitting.value = true
  try {
    // 当前后端接口只支持单商品下单 → 需逐个调用
    // 建议未来后端提供批量接口：createBatchOrder({ items: [{productId, quantity, addressId}] })
    const promises = orderItems.value.map(item => {
      const dto: CreateOrderDto = {
        productId: item.productId,
        quantity: item.quantity || 1,
        addressId: selectedAddressId.value!
      }
      return createOrder(dto)
    })

    const results = await Promise.all(promises)

    const successCount = results.filter(r => r?.orderNo).length

    ElMessage.success(`成功创建 ${successCount} 个订单`)

    if (successCount === 1 && results[0]?.orderNo) {
      router.replace(`/pay/${results[0].orderNo}`)
    } else {
      router.replace('/user/orders')
    }
  } catch (err) {
    console.error('创建订单失败', err)
    // 错误提示建议由 axios 拦截器统一处理
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