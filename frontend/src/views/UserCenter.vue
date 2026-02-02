<template>
  <div class="user-center">
    <div class="container">
      <div class="user-profile-card">
        <el-avatar :size="80" :src="getFullImageUrl(authStore.user.avatar) || defaultAvatar" />
        <div class="info">
          <h2 class="nickname">{{ authStore.user?.nickname || '未登录用户' }}</h2>
          <p class="email">{{ authStore.user?.email || '暂无邮箱' }}</p>
        </div>
        <div class="stats">
          <div class="stat-item">
            <div class="num">0</div>
            <div class="label">获赞</div>
          </div>
          <div class="stat-item">
            <div class="num">0</div>
            <div class="label">积分</div>
          </div>
        </div>
      </div>

      <div class="main-tabs">
        <el-tabs v-model="activeTab" class="custom-tabs">
          <el-tab-pane label="我买到的" name="buying">
            <div class="order-list">
              <el-empty v-if="buyerOrders.length === 0" description="暂无购买记录" />
              <div v-for="order in buyerOrders" :key="order.id" class="order-item">
                <div class="header">
                  <span class="no">订单号：{{ order.orderNo }}</span>
                  <span class="status" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span>
                </div>
                <div class="content">
                  <div class="info">
                    <div class="price">¥ {{ order.totalPrice }}</div>
                    <div class="time">下单时间：{{ formatDate(order.createTime) }}</div>
                  </div>
                  <div class="actions">
                    <el-button v-if="order.status === 1" type="primary" size="small" @click="handlePay(order)">去支付</el-button>
                    <el-button v-if="order.status === 3" type="success" size="small" @click="handleReceive(order)">确认收货</el-button>
                    <el-button v-if="[1,2].includes(order.status)" size="small" @click="handleCancel(order)">取消订单</el-button>
                    <el-button v-if="order.status === 4" size="small" @click="openCommentDialog(order)">评价</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="我卖出的" name="selling">
            <div class="order-list">
              <el-empty v-if="sellerOrders.length === 0" description="暂无出售记录" />
              <div v-for="order in sellerOrders" :key="order.id" class="order-item">
                <div class="header">
                  <span class="no">订单号：{{ order.orderNo }}</span>
                  <span class="status" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span>
                </div>
                <div class="content">
                  <div class="info">
                    <div class="price">收款：¥ {{ order.totalPrice }}</div>
                    <div class="address">发给：{{ order.receiverName }} ({{ order.receiverAddress }})</div>
                  </div>
                  <div class="actions">
                    <el-button v-if="order.status === 2" type="primary" size="small" @click="openDeliverDialog(order)">去发货</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="我发布的" name="products">
            <div class="my-products">
              <el-empty v-if="myProducts.length === 0" description="暂无发布" />
              <div v-for="prod in myProducts" :key="prod.id" class="prod-item">
                <el-image 
                  :src="(prod.images && prod.images.length > 0) ? prod.images[0] : defaultAvatar" 
                  class="thumb" fit="cover" @click="router.push(`/product/${prod.id}`)"
                />
                <div class="detail">
                  <div class="main-info">
                    <div class="title">{{ prod.title }}</div>
                    <div class="price">¥ {{ prod.price }}</div>
                    <el-tag size="small" :type="prod.productStatus === 2 ? 'success' : 'info'">
                      {{ prod.productStatus === 2 ? '展示中' : '已下架' }}
                    </el-tag>
                  </div>
                  <div class="prod-actions">
                    <el-button link type="primary" @click="handleToggleStatus(prod)">
                      {{ prod.productStatus === 2 ? '下架' : '上架' }}
                    </el-button>
                    <el-button link type="danger" @click="handleDeleteProduct(prod.id)">删除</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="地址管理" name="address">
            <div class="address-manage">
              <el-button type="primary" :icon="Plus" style="margin-bottom: 16px" @click="openAddressDialog()">新增地址</el-button>
              <el-table :data="addressList" stripe style="width: 100%">
                <el-table-column prop="receiverName" label="收货人" width="100" />
                <el-table-column prop="receiverPhone" label="电话" width="120" />
                <el-table-column label="地址">
                  <template #default="scope">
                    {{ scope.row.province }}{{ scope.row.city }}{{ scope.row.district }}{{ scope.row.detailAddress }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button link type="primary" size="small" @click="openAddressDialog(scope.row)">编辑</el-button>
                    <el-button link type="danger" size="small" @click="handleDeleteAddress(scope.row.id)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-dialog v-model="addressDialogVisible" :title="isEditAddress ? '修改地址' : '新增地址'" width="500px">
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="addressForm.receiverName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="addressForm.receiverPhone" /></el-form-item>
        <el-form-item label="省市区">
          <el-input v-model="addressForm.province" placeholder="省" style="width: 32%" />
          <el-input v-model="addressForm.city" placeholder="市" style="width: 32%" />
          <el-input v-model="addressForm.district" placeholder="区" style="width: 32%" />
        </el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addressForm.detailAddress" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deliverDialogVisible" title="填写发货信息" width="400px">
      <el-input v-model="trackingNo" placeholder="请输入物流单号" />
      <template #footer>
        <el-button @click="deliverDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDeliver">确认发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="commentDialogVisible" title="评价商品" width="500px">
      <el-form :model="commentForm" label-width="60px">
        <el-form-item label="评分"><el-rate v-model="commentForm.score" /></el-form-item>
        <el-form-item label="心得"><el-input v-model="commentForm.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getFullImageUrl } from '@/utils/image'

// API 导入
import { getBuyerOrders, getSellerOrders, deliverOrder, confirmOrder, cancelOrder } from '@/api/order'
import { getAddressList, addAddress, deleteAddress, updateAddress } from '@/api/address'
import { getMyProducts, updateProductStatus, deleteProduct } from '@/api/product'
import { publishComment } from '@/api/comment'

// DTO 导入
import type { OrderDto } from '@/dto/order'
import type { AddressDto } from '@/dto/address'
import type { ProductDto } from '@/dto/product'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('buying')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 数据列表
const buyerOrders = ref<OrderDto[]>([])
const sellerOrders = ref<OrderDto[]>([])
const myProducts = ref<ProductDto[]>([])
const addressList = ref<AddressDto[]>([])

// 弹窗状态
const deliverDialogVisible = ref(false)
const addressDialogVisible = ref(false)
const commentDialogVisible = ref(false)
const isEditAddress = ref(false)

// 表单
const currentOrderNo = ref('')
const trackingNo = ref('')
const addressForm = reactive<AddressDto>({ id: undefined, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0 })
const commentForm = reactive({ orderId: 0, score: 5, content: '' })

// 加载数据
const loadData = async () => {
  try {
    const [res1, res2, res3, resAddr] = await Promise.all([
      getBuyerOrders({ page: 1, size: 20 }),
      getSellerOrders({ page: 1, size: 20 }),
      getMyProducts({ page: 1, size: 20 }),
      getAddressList()
    ])
    buyerOrders.value = (res1 as any)?.list || []
    sellerOrders.value = (res2 as any)?.list || []
    myProducts.value = (res3 as any)?.list || (Array.isArray(res3) ? res3 : [])
    addressList.value = Array.isArray(resAddr) ? resAddr : (resAddr as any)?.list || []
  } catch (error) { console.error(error) }
}

// 地址逻辑
const openAddressDialog = (row?: AddressDto) => {
  if (row) {
    isEditAddress.value = true
    Object.assign(addressForm, { ...row })
  } else {
    isEditAddress.value = false
    Object.assign(addressForm, { id: undefined, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0 })
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  if (!addressForm.receiverName || !addressForm.receiverPhone) return ElMessage.warning('请填写必填项')
  try {
    if (isEditAddress.value) {
      await updateAddress(addressForm)
      ElMessage.success('修改成功')
    } else {
      await addAddress({ ...addressForm })
      ElMessage.success('添加成功')
    }
    addressDialogVisible.value = false
    await loadData()
  } catch (e) { console.error(e) }
}

const handleDeleteAddress = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
    await deleteAddress(id)
    loadData()
  } catch (e) {}
}

// 商品逻辑
const handleToggleStatus = async (prod: ProductDto) => {
  const isOffshelf = prod.productStatus === 2
  const actionText = isOffshelf ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${actionText}该商品吗？`, '提示', { type: 'warning' })
    await updateProductStatus({ ...prod, status: isOffshelf ? 3 : 2 })
    ElMessage.success(`${actionText}成功`)
    loadData()
  } catch (e) {}
}

const handleDeleteProduct = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除商品吗？', '警告', { type: 'error' })
    await deleteProduct(id)
    loadData()
  } catch (e) {}
}

// 订单/辅助逻辑 (此处保持原有实现)
const getStatusText = (s: number) => ({ 1: '待支付', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消' }[s] || '未知')
const getStatusClass = (s: number) => ({ 1: 'text-danger', 2: 'text-warning', 3: 'text-primary', 4: 'text-success' }[s] || 'text-gray')
const formatDate = (str: string) => str ? str.replace('T', ' ').split('.')[0] : ''
const handlePay = (o: OrderDto) => router.push(`/pay/${o.orderNo}`)
const openDeliverDialog = (o: OrderDto) => { currentOrderNo.value = o.orderNo; trackingNo.value = ''; deliverDialogVisible.value = true }
const handleDeliver = async () => { await deliverOrder({ orderNo: currentOrderNo.value, trackingNo: trackingNo.value }); deliverDialogVisible.value = false; loadData() }
const handleReceive = async (o: OrderDto) => { await confirmOrder(o.orderNo); loadData() }
const handleCancel = async (o: OrderDto) => { await ElMessageBox.confirm('取消订单？'); await cancelOrder(o.orderNo); loadData() }
const openCommentDialog = (o: OrderDto) => { commentForm.orderId = o.id; commentDialogVisible.value = true }
const submitComment = async () => { await publishComment(commentForm); commentDialogVisible.value = false; loadData() }

onMounted(() => { loadData() })
</script>




<style scoped lang="scss">
.user-center {
  background-color: #f9fafb;
  min-height: calc(100vh - 64px);
  padding: 40px 20px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
}

.user-profile-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  
  .info {
    flex: 1;
    .nickname { margin: 0 0 4px; font-size: 24px; color: #111827; }
    .email { margin: 0; color: #6b7280; }
  }
  
  .stats {
    display: flex;
    gap: 40px;
    padding-right: 20px;
    
    .stat-item {
      text-align: center;
      .num { font-size: 20px; font-weight: bold; color: #10b981; }
      .label { font-size: 12px; color: #6b7280; }
    }
  }
}

.main-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  min-height: 500px;
}

/* 订单列表样式 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  .order-item {
    border: 1px solid #f3f4f6;
    border-radius: 8px;
    overflow: hidden;
    
    .header {
      background: #f9fafb;
      padding: 10px 16px;
      display: flex;
      justify-content: space-between;
      font-size: 13px;
      color: #6b7280;
      
      .status { font-weight: 600; }
    }
    
    .content {
      padding: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .info {
        .price { font-size: 16px; font-weight: bold; color: #111827; }
        .time, .address { font-size: 12px; color: #9ca3af; margin-top: 4px; }
      }
    }
  }
}

.my-products {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  
  .prod-item {
    display: flex;
    gap: 12px;
    border: 1px solid #f3f4f6;
    padding: 10px;
    border-radius: 8px;
    cursor: pointer;
    &:hover { background: #f9fafb; }
    
    .thumb { width: 80px; height: 80px; border-radius: 4px; }
    .detail {
      flex: 1;
      .title { font-size: 14px; margin-bottom: 4px; }
      .price { color: #ef4444; font-weight: bold; }
    }
  }
}
.user-center { padding: 20px; background: #f8f9fa; min-height: 100vh; }
.container { max-width: 1100px; margin: 0 auto; }
.user-profile-card { display: flex; align-items: center; background: #fff; padding: 30px; border-radius: 12px; margin-bottom: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.user-profile-card .info { margin-left: 20px; flex: 1; }
.nickname { margin: 0; font-size: 24px; }
.email { color: #909399; margin-top: 5px; }
.stats { display: flex; gap: 30px; text-align: center; }
.num { font-size: 20px; font-weight: bold; color: #303133; }
.label { font-size: 12px; color: #909399; }
.order-item { background: #fff; border-radius: 8px; padding: 15px; margin-bottom: 15px; border: 1px solid #ebeef5; }
.order-item .header { display: flex; justify-content: space-between; border-bottom: 1px solid #f2f6fc; padding-bottom: 10px; margin-bottom: 10px; }
.order-item .content { display: flex; justify-content: space-between; align-items: center; }
.price { color: #f56c6c; font-size: 18px; font-weight: bold; }
.time, .address { font-size: 13px; color: #606266; margin-top: 5px; }
.prod-item { display: flex; padding: 15px; background: #fff; border-radius: 8px; margin-bottom: 10px; cursor: pointer; border: 1px solid #ebeef5; transition: 0.3s; }
.prod-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.thumb { width: 80px; height: 80px; border-radius: 4px; flex-shrink: 0; }
.detail { margin-left: 15px; flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.title { font-weight: bold; font-size: 16px; }
.text-danger { color: #f56c6c; }
.text-warning { color: #e6a23c; }
.text-primary { color: #409eff; }
.text-success { color: #67c23a; }

/* 状态颜色 */
.text-danger { color: #ef4444; }
.text-warning { color: #f59e0b; }
.text-success { color: #10b981; }
.text-primary { color: #3b82f6; }
.text-gray { color: #9ca3af; }

.user-center { padding: 20px; background: #f8f9fa; min-height: 100vh; }
.container { max-width: 1000px; margin: 0 auto; }
.user-profile-card { display: flex; align-items: center; background: #fff; padding: 25px; border-radius: 12px; margin-bottom: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.info { margin-left: 20px; flex: 1; }
.stats { display: flex; gap: 20px; }
.stat-item { text-align: center; }
.num { font-weight: bold; font-size: 18px; }
.label { font-size: 12px; color: #999; }
.order-item, .prod-item { background: #fff; border-radius: 8px; padding: 15px; margin-bottom: 15px; border: 1px solid #eee; }
.header { display: flex; justify-content: space-between; border-bottom: 1px solid #f5f5f5; padding-bottom: 10px; margin-bottom: 10px; }
.content { display: flex; justify-content: space-between; align-items: center; }
.prod-item { display: flex; }
.thumb { width: 80px; height: 80px; border-radius: 4px; }
.detail { margin-left: 15px; flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.prod-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 10px; }
.text-danger { color: #F56C6C; }
.text-warning { color: #E6A23C; }
.text-primary { color: #409EFF; }
.text-success { color: #67C23A; }
</style>