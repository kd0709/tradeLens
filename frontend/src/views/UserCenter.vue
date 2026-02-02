<template>
  <div class="user-center">
    <div class="container">
      <div class="user-profile-card">
        <el-avatar :size="80" :src="authStore.user?.avatar || defaultAvatar" />
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
                  <span class="status" :class="getStatusClass(order.status)">
                    {{ getStatusText(order.status) }}
                  </span>
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
                        
                        <el-button 
                            v-if="order.status === 4" 
                            size="small" 
                            @click="openCommentDialog(order)"
                        >
                            评价
                        </el-button>
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
                  <span class="status" :class="getStatusClass(order.status)">
                    {{ getStatusText(order.status) }}
                  </span>
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
               <div v-for="prod in myProducts" :key="prod.id" class="prod-item" @click="router.push(`/product/${prod.id}`)">
                 <el-image :src="prod.images[0]" class="thumb" fit="cover"/>
                 <div class="detail">
                   <div class="title">{{ prod.title }}</div>
                   <div class="price">¥ {{ prod.price }}</div>
                   <el-tag size="small">{{ prod.productStatus === 2 ? '已上架' : '已下架/售出' }}</el-tag>
                 </div>
               </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="地址管理" name="address">
            <div class="address-manage">
              <el-button type="primary" icon="Plus" style="margin-bottom: 16px" @click="openAddressDialog()">新增地址</el-button>
              <el-table :data="addressList" stripe style="width: 100%">
                <el-table-column prop="receiverName" label="收货人" width="100" />
                <el-table-column prop="receiverPhone" label="电话" width="120" />
                <el-table-column label="地址">
                  <template #default="scope">
                    {{ scope.row.province }} {{ scope.row.city }} {{ scope.row.detailAddress }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="scope">
                    <el-button link type="danger" size="small" @click="handleDeleteAddress(scope.row.id)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-dialog v-model="deliverDialogVisible" title="填写发货信息" width="400px">
      <el-input v-model="trackingNo" placeholder="请输入物流单号" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deliverDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleDeliver">确认发货</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="addressDialogVisible" title="新增地址" width="500px">
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人">
          <el-input v-model="addressForm.receiverName" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="addressForm.receiverPhone" />
        </el-form-item>
        <el-form-item label="省/市/区">
          <el-input v-model="addressForm.province" placeholder="省" style="width: 30%"/>
          <el-input v-model="addressForm.city" placeholder="市" style="width: 30%"/>
          <el-input v-model="addressForm.district" placeholder="区" style="width: 30%"/>
        </el-form-item>
        <el-form-item label="详细地址">
           <el-input v-model="addressForm.detailAddress" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="commentDialogVisible" title="评价商品" width="500px">
      <el-form :model="commentForm" label-width="60px">
        <el-form-item label="评分">
          <el-rate v-model="commentForm.score" />
        </el-form-item>
        <el-form-item label="心得">
          <el-input 
            v-model="commentForm.content" 
            type="textarea" 
            :rows="3" 
            placeholder="商品满足你的期待吗？说说你的使用心得..." 
          />
        </el-form-item>
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
import { getBuyerOrders, getSellerOrders, payOrder, deliverOrder, confirmOrder, cancelOrder } from '@/api/order'
import { getAddressList, addAddress, deleteAddress } from '@/api/address'
import { getMyProducts } from '@/api/product'
import { publishComment } from '@/api/comment'
import type { OrderDto } from '@/dto/order'
import type { AddressDto } from '@/dto/address'
import type { ProductDto } from '@/dto/product'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('buying')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 数据状态
const buyerOrders = ref<OrderDto[]>([])
const sellerOrders = ref<OrderDto[]>([])
const myProducts = ref<ProductDto[]>([])
const addressList = ref<AddressDto[]>([])

// 发货相关
const deliverDialogVisible = ref(false)
const currentOrderId = ref<number>(0)
const currentOrderNo = ref('')
const trackingNo = ref('')

// 地址表单
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

// 评价相关
const commentDialogVisible = ref(false)
const commentForm = reactive({
  orderId: 0,
  score: 5,
  content: ''
})

// 打开评价弹窗
const openCommentDialog = (order: OrderDto) => {
  commentForm.orderId = order.id
  commentForm.score = 5
  commentForm.content = ''
  commentDialogVisible.value = true
}

// 提交评价
const submitComment = async () => {
  if (!commentForm.content.trim()) return ElMessage.warning('请输入评价内容')
  try {
    await publishComment(commentForm)
    ElMessage.success('评价成功')
    commentDialogVisible.value = false
    loadData() // 刷新列表，更新订单状态
  } catch (e) { console.error(e) }
}

// 状态文本辅助
const getStatusText = (status: number) => {
  const map: Record<number, string> = { 1: '待支付', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消' }
  return map[status] || '未知'
}
const getStatusClass = (status: number) => {
  if (status === 1) return 'text-danger'
  if (status === 2) return 'text-warning'
  if (status === 3) return 'text-primary'
  if (status === 4) return 'text-success'
  return 'text-gray'
}
const formatDate = (str: string) => str ? str.replace('T', ' ').split('.')[0] : ''

// 加载数据
const loadData = async () => {
  try {
    // 加载买家订单（后端返回PageDto格式，包含list字段）
    const res1 = await getBuyerOrders({ page: 1, size: 20 })
    buyerOrders.value = res1.list || []
    
    // 加载卖家订单
    const res2 = await getSellerOrders({ page: 1, size: 20 })
    sellerOrders.value = res2.list || []

    // 加载我的发布（后端返回PageDto格式）
    const res3 = await getMyProducts({ page: 1, size: 20 })
    myProducts.value = res3.list || []

    // 加载地址
    addressList.value = await getAddressList()
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 订单操作
const handlePay = (order: OrderDto) => {
  // 跳转到收银台页面，使用orderNo
  router.push(`/pay/${order.orderNo}`)
}

const handleCancel = async (order: OrderDto) => {
  try {
    await ElMessageBox.confirm('确定取消该订单吗?', '提示', { type: 'warning' })
    await cancelOrder(order.orderNo)  // 使用orderNo而非id
    ElMessage.success('订单已取消')
    loadData()
  } catch (e) { console.error(e) }
}

const handleReceive = async (order: OrderDto) => {
  try {
    await ElMessageBox.confirm('确认收到货物了吗?', '提示', { type: 'success' })
    await confirmOrder(order.orderNo)  // 使用orderNo而非id
    ElMessage.success('交易完成！')
    loadData()
  } catch (e) { console.error(e) }
}

// 发货逻辑
const openDeliverDialog = (order: OrderDto) => {
  currentOrderNo.value = order.orderNo
  trackingNo.value = ''
  deliverDialogVisible.value = true
}

const handleDeliver = async () => {
  if (!trackingNo.value) return ElMessage.warning('请填写单号')
  try {
    await deliverOrder({ orderNo: currentOrderNo.value, trackingNo: trackingNo.value })
    ElMessage.success('发货成功')
    deliverDialogVisible.value = false
    loadData()
  } catch (e) { console.error(e) }
}

// 地址逻辑
const openAddressDialog = () => {
  // 重置表单...
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  try {
    await addAddress({ ...addressForm })
    ElMessage.success('地址添加成功')
    addressDialogVisible.value = false
    loadData()
  } catch(e) { console.error(e) }
}

const handleDeleteAddress = async (id: number) => {
  if(!id) return
  await deleteAddress(id)
  loadData()
}

onMounted(() => {
  loadData()
})
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

/* 状态颜色 */
.text-danger { color: #ef4444; }
.text-warning { color: #f59e0b; }
.text-success { color: #10b981; }
.text-primary { color: #3b82f6; }
.text-gray { color: #9ca3af; }
</style>