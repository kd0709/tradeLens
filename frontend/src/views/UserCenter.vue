<template>
  <div class="user-center-page">
    <div class="bg-decoration left"></div>
    <div class="bg-decoration right"></div>

    <div class="container">
      <Transition name="fade-down" appear>
        <div class="user-profile-header">
          <div class="profile-content">
            <el-avatar
              class="user-avatar"
              :size="88"
              :src="getFullImageUrl(authStore.user?.avatar) || defaultAvatar"
            />
            <div class="info-block">
              <h2 class="nickname">
                {{ authStore.user?.nickname || '未登录用户' }}
                <el-tag size="small" effect="plain" round class="role-tag">普通用户</el-tag>
              </h2>
              <p class="email">{{ authStore.user?.email || '暂无邮箱' }}</p>
            </div>
            
            <div class="stats-block">
              <div class="stat-item">
                <div class="num">{{ myProducts.length }}</div>
                <div class="label">发布</div>
              </div>
              <div class="stat-item">
                <div class="num">{{ buyerOrders.length }}</div>
                <div class="label">买入</div>
              </div>
              <div class="stat-item">
                <div class="num">{{ sellerOrders.length }}</div>
                <div class="label">卖出</div>
              </div>
            </div>
          </div>
        </div>
      </Transition>

      <Transition name="fade-up" appear>
        <div class="main-card glass-panel">
          <el-tabs v-model="activeTab" class="custom-tabs">
            
            <el-tab-pane label="基本信息" name="profile">
              <div class="tab-content-wrapper profile-form-wrapper">
                <div class="form-section">
                  <h3 class="section-title">编辑资料</h3>
                  <el-form 
                    :model="userInfoForm" 
                    label-position="top" 
                    size="large"
                    class="user-form"
                  >
                    <el-form-item label="头像">
                      <el-upload
                        class="avatar-uploader"
                        action="#"
                        :show-file-list="false"
                        :http-request="handleAvatarUpload"
                      >
                        <img v-if="userInfoForm.avatar" :src="getFullImageUrl(userInfoForm.avatar)" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                        <div class="upload-tip">点击修改头像</div>
                      </el-upload>
                    </el-form-item>

                    <el-form-item label="昵称">
                      <el-input v-model="userInfoForm.nickname" placeholder="请输入昵称" />
                    </el-form-item>
                    
                    <el-form-item label="电话">
                      <el-input v-model="userInfoForm.phone" placeholder="请输入电话" />
                    </el-form-item>

                    <el-form-item label="邮箱">
                      <el-input v-model="userInfoForm.email" placeholder="请输入邮箱" />
                    </el-form-item>

                    <el-form-item>
                      <el-button type="primary" :loading="loading" @click="handleUpdateProfile">保存修改</el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我买到的" name="buying">
              <div class="tab-content-wrapper">
                <el-empty v-if="buyerOrders.length === 0" description="暂无购买记录" />
                <div class="order-list" v-else>
                  <div v-for="order in buyerOrders" :key="order.id" class="order-card">
                    <div class="card-header">
                      <span class="order-no">订单号：{{ order.orderNo }}</span>
                      <span class="status-badge" :class="getStatusClass(order.status)">
                        {{ getStatusText(order.status) }}
                      </span>
                    </div>
                    <div class="card-body">
                      <div class="info-row">
                        <div class="price-box">
                          <span class="label">实付金额</span>
                          <span class="value">¥ {{ order.totalPrice }}</span>
                        </div>
                        <div class="time-box">下单时间：{{ formatDate(order.createTime) }}</div>
                      </div>
                      <div class="action-row">
                        <el-button v-if="order.status === 1" type="primary" round size="small" @click="handlePay(order)">去支付</el-button>
                        <el-button v-if="order.status === 3" type="success" round size="small" @click="handleReceive(order)">确认收货</el-button>
                        <el-button v-if="[1,2].includes(order.status)" round size="small" @click="handleCancel(order)">取消订单</el-button>
                        <el-button v-if="order.status === 4" round size="small" @click="openCommentDialog(order)">评价</el-button>
                        <el-button v-if="[4,5].includes(order.status)" text size="small">删除记录</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我卖出的" name="selling">
              <div class="tab-content-wrapper">
                <el-empty v-if="sellerOrders.length === 0" description="暂无出售记录" />
                <div class="order-list" v-else>
                  <div v-for="order in sellerOrders" :key="order.id" class="order-card">
                    <div class="card-header">
                      <span class="order-no">订单号：{{ order.orderNo }}</span>
                      <span class="status-badge" :class="getStatusClass(order.status)">
                        {{ getStatusText(order.status) }}
                      </span>
                    </div>
                    <div class="card-body">
                      <div class="info-row">
                        <div class="price-box">
                          <span class="label">预计收入</span>
                          <span class="value success">¥ {{ order.totalPrice }}</span>
                        </div>
                        <div class="address-box">
                          <el-icon><Location /></el-icon>
                          {{ order.receiverName }} | {{ order.receiverAddress }}
                        </div>
                      </div>
                      <div class="action-row">
                        <el-button v-if="order.status === 2" type="primary" round size="small" @click="openDeliverDialog(order)">去发货</el-button>
                        <span v-else class="wait-text">暂无操作</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我发布的" name="products">
              <div class="tab-content-wrapper">
                <el-empty v-if="myProducts.length === 0" description="暂无发布" />
                <div class="product-grid" v-else>
                  <div v-for="prod in myProducts" :key="prod.id" class="manage-product-card">
                    <div class="image-area" @click="router.push(`/product/${prod.id}`)">
                      <el-image :src="getFullImageUrl(prod.mainImage)" fit="cover" loading="lazy" />
                      <div class="status-overlay" v-if="prod.productStatus !== 2">
                        {{ prod.productStatus === 4 ? '已售出' : '已下架' }}
                      </div>
                    </div>
                    <div class="info-area">
                      <div class="title" :title="prod.title">{{ prod.title }}</div>
                      <div class="price">¥ {{ prod.price }}</div>
                      <div class="actions">
                        <el-button 
                          size="small" 
                          :type="prod.productStatus === 2 ? 'warning' : 'success'" 
                          link
                          @click="handleToggleStatus(prod)"
                        >
                          {{ prod.productStatus === 2 ? '下架' : '上架' }}
                        </el-button>
                        <el-divider direction="vertical" />
                        <el-button size="small" type="danger" link @click="handleDeleteProduct(prod.id)">删除</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="地址管理" name="address">
              <div class="tab-content-wrapper">
                <div class="address-grid">
                  <div class="address-card add-card" @click="openAddressDialog()">
                    <el-icon class="icon"><Plus /></el-icon>
                    <span>新增收货地址</span>
                  </div>
                  
                  <div v-for="addr in addressList" :key="addr.id" class="address-card">
                    <div class="addr-header">
                      <span class="name">{{ addr.receiverName }}</span>
                      <span class="phone">{{ addr.receiverPhone }}</span>
                    </div>
                    <div class="addr-body">
                      <p>{{ addr.province }} {{ addr.city }} {{ addr.district }}</p>
                      <p class="detail">{{ addr.detailAddress }}</p>
                    </div>
                    <div class="addr-actions">
                      <el-tag v-if="addr.isDefault" size="small" type="success">默认</el-tag>
                      <div class="btns">
                        <el-button link type="primary" size="small" @click="openAddressDialog(addr)">编辑</el-button>
                        <el-button link type="danger" size="small" @click="handleDeleteAddress(addr.id)">删除</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="销售分析" name="stats">
              <div class="tab-content-wrapper stats-wrapper">
                <div class="stats-summary-grid">
                  <div class="stat-card gradient-blue">
                    <div class="icon-bg"><el-icon><Money /></el-icon></div>
                    <div class="content">
                      <div class="label">累计销售额</div>
                      <div class="value">¥ {{ statsData.totalAmount }}</div>
                    </div>
                  </div>
                  <div class="stat-card gradient-green">
                    <div class="icon-bg"><el-icon><Goods /></el-icon></div>
                    <div class="content">
                      <div class="label">成交订单数</div>
                      <div class="value">{{ statsData.totalOrders }} 单</div>
                    </div>
                  </div>
                  <div class="stat-card gradient-purple">
                    <div class="icon-bg"><el-icon><Wallet /></el-icon></div>
                    <div class="content">
                      <div class="label">客单价</div>
                      <div class="value">¥ {{ statsData.avgPrice }}</div>
                    </div>
                  </div>
                </div>

                <div class="charts-grid">
                  <div class="chart-card line-chart">
                    <div class="chart-header">
                      <span class="title">近7天销售趋势</span>
                    </div>
                    <div ref="trendChartRef" class="echart-box"></div>
                  </div>
                  <div class="chart-card pie-chart">
                    <div class="chart-header">
                      <span class="title">商品类别占比</span>
                    </div>
                    <div ref="categoryChartRef" class="echart-box"></div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

          </el-tabs>
        </div>
      </Transition>
    </div>

    <el-dialog v-model="addressDialogVisible" :title="isEditAddress ? '修改地址' : '新增地址'" width="500px">
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="addressForm.receiverName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="addressForm.receiverPhone" /></el-form-item>
        <el-form-item label="省市区">
          <div style="display: flex; gap: 10px; width: 100%">
            <el-input v-model="addressForm.province" placeholder="省" />
            <el-input v-model="addressForm.city" placeholder="市" />
            <el-input v-model="addressForm.district" placeholder="区" />
          </div>
        </el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addressForm.detailAddress" /></el-form-item>
        <el-form-item label="设为默认">
           <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
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
import { ref, onMounted, reactive, watch, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Location, Money, Goods, Wallet } from '@element-plus/icons-vue' // 补全图标引入
import { getFullImageUrl } from '@/utils/image'
import * as echarts from 'echarts'

// API
import { getBuyerOrders, getSellerOrders, deliverOrder, confirmOrder, cancelOrder } from '@/api/order'
import { getMyAddressList, deleteAddress, updateAddress, createAddress } from '@/api/address'
import { getMyProducts, updateProductStatus, deleteProduct } from '@/api/product'
import { getSalesStatistics } from '@/api/statistics'
import { publishComment } from '@/api/comment'
import { updateUserInfo } from '@/api/user'
import { uploadFile } from '@/api/common'

// DTO
import type { OrderDto } from '@/dto/order'
import type { SalesStatisticsDto } from '@/dto/statistics'
import type { AddressDto, AddressUpdateDto } from '@/dto/address'
import type { ProductMyDto } from '@/dto/product'
import type { CommentPublishDto } from '@/dto/comment'
import type { UploadRequestOptions } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('profile')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const loading = ref(false)

// 数据列表
const buyerOrders = ref<OrderDto[]>([])
const sellerOrders = ref<OrderDto[]>([])
const myProducts = ref<ProductMyDto[]>([])
const addressList = ref<AddressDto[]>([])

// --- 统计模块相关变量 ---
const trendChartRef = ref<HTMLElement | null>(null)
const categoryChartRef = ref<HTMLElement | null>(null)
let trendChartInstance: echarts.ECharts | null = null
let categoryChartInstance: echarts.ECharts | null = null
const cachedStats = ref<SalesStatisticsDto | null>(null)

// 核心指标数据
const statsData = reactive({
  totalAmount: '0.00',
  totalOrders: 0,
  avgPrice: '0.00'
})

// 表单数据
const userInfoForm = reactive({
  avatar: authStore.user?.avatar || '',
  phone: authStore.user?.phone || '',
  nickname: authStore.user?.nickname || '',
  email: authStore.user?.email || ''
})

const addressForm = reactive<AddressUpdateDto>({ 
  id: 0, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0 
})
const commentForm = reactive<CommentPublishDto>({ orderId: 0, score: 5, content: '' })
const currentOrderNo = ref('')
const trackingNo = ref('')

// 弹窗状态
const deliverDialogVisible = ref(false)
const addressDialogVisible = ref(false)
const commentDialogVisible = ref(false)
const isEditAddress = ref(false)

// --- 1. 用户信息相关 ---
const initUserInfo = () => {
  if (authStore.user) {
    userInfoForm.avatar = authStore.user.avatar || ''
    userInfoForm.nickname = authStore.user.nickname || ''
    userInfoForm.email = authStore.user.email || ''
  }
}

watch(() => authStore.user, () => { initUserInfo() }, { immediate: true })

const handleUpdateProfile = async () => {
  if (!userInfoForm.nickname) return ElMessage.warning('昵称不能为空')
  loading.value = true
  try {
    await updateUserInfo({ ...userInfoForm })
    if (authStore.user) {
      Object.assign(authStore.user, userInfoForm)
      localStorage.setItem('user', JSON.stringify(authStore.user))
    }
    ElMessage.success('个人资料已更新')
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    loading.value = false
  }
}

const handleAvatarUpload = async (options: UploadRequestOptions) => {
  try {
    const res = await uploadFile(options.file)
    let url = ''
    if (typeof res === 'string') url = res
    else if (res && typeof res === 'object') url = (res as any).url || (res as any).data
    if (url) {
      userInfoForm.avatar = url
      ElMessage.success('头像上传成功，记得保存哦')
    }
  } catch (error) { ElMessage.error('图片上传失败') }
}

// --- 2. 统计图表逻辑 ---
const initCharts = () => {
  if (!trendChartRef.value || !categoryChartRef.value) return
  if (!cachedStats.value) return

  const data = cachedStats.value

  // 渲染趋势图
  if (!trendChartInstance) trendChartInstance = echarts.init(trendChartRef.value)
  
  const xAxisData = data.last7DaysTrend.map(item => item.date)
  const seriesData = data.last7DaysTrend.map(item => item.amount)

  trendChartInstance.setOption({
    tooltip: { trigger: 'axis', formatter: '{b} <br/>销售额: ¥{c}' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData,
      axisLine: { lineStyle: { color: '#9ca3af' } }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#e5e7eb' } }
    },
    series: [{
      name: '销售额',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#10b981' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(16, 185, 129, 0.4)' },
          { offset: 1, color: 'rgba(16, 185, 129, 0.01)' }
        ])
      },
      data: seriesData
    }]
  })

  // 渲染占比图
  if (!categoryChartInstance) categoryChartInstance = echarts.init(categoryChartRef.value)
  
  categoryChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', left: 'center', itemWidth: 10, itemHeight: 10 },
    series: [{
      name: '商品类别',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: data.categoryStats
    }]
  })
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await getSalesStatistics()
    console.log('统计数据', res)
    if (res) {
      cachedStats.value = res
      statsData.totalAmount = res.sellerData.totalAmount.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
      statsData.totalOrders = res.sellerData.totalOrders
      statsData.avgPrice = res.avgPrice.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
      
      if (activeTab.value === 'stats') {
        nextTick(() => initCharts())
      }
    }
  } catch (error) { console.error('获取统计数据失败', error) }
}

// --- 3. 数据加载与生命周期 ---
const loadData = async () => {
  try {
    const [res1, res2, res3, resAddr] = await Promise.all([
      getBuyerOrders({ current: 1, size: 20 }),
      getSellerOrders({ current: 1, size: 20 }),
      getMyProducts({ current: 1, size: 20 }),
      getMyAddressList(),
      fetchStatistics() // 并在加载时获取统计数据
    ])
    buyerOrders.value = (res1 as any)?.list || []
    sellerOrders.value = (res2 as any)?.list || []
    myProducts.value = (res3 as any)?.list || (Array.isArray(res3) ? res3 : [])
    addressList.value = Array.isArray(resAddr) ? resAddr : (resAddr as any)?.list || []
  } catch (error) { console.error(error) }
}

// 辅助函数
const getStatusText = (s: number) => ({ 
  1: '待支付', 
  2: '待发货', 
  3: '已发货', 4: '已完成', 
  5: '已取消' }[s] || '未知'
)

const getStatusClass = (s: number) => ({ 
  1: 'status-danger', 
  2: 'status-warning', 
  3: 'status-primary', 
  4: 'status-success' }[s] || 'status-gray'
)

const formatDate = (str: string) => str ? str.replace('T', ' ').split('.')[0] : ''

// 订单操作
const handlePay = (o: OrderDto) => router.push(`/pay/${o.orderNo}`)
const openDeliverDialog = (o: OrderDto) => { 
  currentOrderNo.value = o.orderNo; 
  trackingNo.value = ''; 
  deliverDialogVisible.value = true 
}
const handleDeliver = async () => { 
  await deliverOrder({ orderNo: currentOrderNo.value, 
    trackingNo: trackingNo.value 
  }); 
  deliverDialogVisible.value = false; 
  loadData() 
}

const handleReceive = async (o: OrderDto) => { 
  await confirmOrder(o.orderNo); 
  loadData() 
}

const handleCancel = async (o: OrderDto) => { 
  await ElMessageBox.confirm('确定取消该订单？'); 
  await cancelOrder(o.orderNo); 
  loadData() 
}

const openCommentDialog = (o: OrderDto) => { 
  commentForm.orderId = o.id; 
  commentDialogVisible.value = true 
}
const submitComment = async () => { 
  await publishComment(commentForm); 
  commentDialogVisible.value = false; 
  loadData() 
}

// 地址操作
const openAddressDialog = (row?: AddressDto) => {
  isEditAddress.value = !!row
  Object.assign(addressForm, row || { id: undefined, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0 })
  addressDialogVisible.value = true
}
const saveAddress = async () => {
  if (!addressForm.receiverName || !addressForm.receiverPhone) return ElMessage.warning('请填写必填项')
  try {
    await (isEditAddress.value ? updateAddress(addressForm) : createAddress({ ...addressForm }))
    addressDialogVisible.value = false; loadData(); ElMessage.success('保存成功')
  } catch (e) {}
}
const handleDeleteAddress = async (id: number) => {
  try { await ElMessageBox.confirm('确定删除吗？'); await deleteAddress(id); loadData() } catch (e) {}
}

// 商品操作
const handleToggleStatus = async (prod: any) => {
  try {
    await ElMessageBox.confirm(`确定要${prod.productStatus === 2 ? '下架' : '上架'}吗？`)
    await updateProductStatus({ id: prod.id, status: prod.productStatus === 2 ? 3 : 2 })
    ElMessage.success('操作成功'); loadData()
  } catch (e) {}
}
const handleDeleteProduct = async (id: number) => {
  try { await ElMessageBox.confirm('确定删除吗？'); await deleteProduct(id); loadData() } catch (e) {}
}

// Watchers & Listeners
watch(activeTab, (newVal) => {
  if (newVal === 'stats') {
    nextTick(() => {
      initCharts()
      setTimeout(() => { trendChartInstance?.resize(); categoryChartInstance?.resize() }, 200)
    })
  }
})

const handleResize = () => { 
  trendChartInstance?.resize(); 
  categoryChartInstance?.resize() 
}

onMounted(() => { 
  loadData();
  window.addEventListener('resize', handleResize) 
})
onUnmounted(() => { 
  window.removeEventListener('resize', handleResize); 
  trendChartInstance?.dispose(); 
  categoryChartInstance?.dispose() 
})
</script>

<style scoped lang="scss">
.user-center-page {
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  min-height: calc(100vh - 64px);
  padding: 40px 20px;
  position: relative;
  overflow: hidden;

  /* 背景装饰 */
  .bg-decoration {
    position: absolute;
    width: 500px;
    height: 500px;
    border-radius: 50%;
    filter: blur(90px);
    opacity: 0.6;
    z-index: 0;
    
    &.left { background: #d1fae5; top: -100px; left: -100px; }
    &.right { background: #e0e7ff; top: 20%; right: -200px; }
  }
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 顶部个人信息卡片 */
.user-profile-header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
  border: 1px solid #fff;

  .profile-content {
    display: flex;
    align-items: center;
    gap: 24px;
    
    .user-avatar { border: 4px solid #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
    
    .info-block {
      flex: 1;
      .nickname {
        font-size: 26px; font-weight: 800; color: #111827; margin: 0 0 8px;
        display: flex; align-items: center; gap: 12px;
      }
      .email { color: #6b7280; font-size: 14px; }
    }
    
    .stats-block {
      display: flex; gap: 48px; padding-right: 20px; border-left: 1px solid #f3f4f6; padding-left: 48px;
      .stat-item {
        text-align: center;
        .num { font-size: 24px; font-weight: 800; color: #10b981; margin-bottom: 4px; }
        .label { font-size: 13px; color: #6b7280; }
      }
    }
  }
}

/* 玻璃面板通用样式 */
.glass-panel {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  border: 1px solid #fff;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  min-height: 600px;
  padding: 24px;
}

/* Tabs 样式优化 */
:deep(.el-tabs__nav-wrap::after) { display: none; }
:deep(.el-tabs__item) {
  font-size: 16px; color: #6b7280; padding: 0 24px; height: 48px; line-height: 48px;
  &.is-active { color: #10b981; font-weight: 700; }
}
:deep(.el-tabs__active-bar) { background-color: #10b981; height: 3px; border-radius: 2px; }

/* 1. 基本信息表单 */
.profile-form-wrapper {
  max-width: 500px;
  margin: 30px auto 0;
  
  .section-title { font-size: 18px; margin-bottom: 24px; color: #374151; font-weight: 600; }
  
  .avatar-uploader {
    text-align: left;
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9; border-radius: 50%; cursor: pointer; position: relative; overflow: hidden; transition: var(--el-transition-duration-fast);
      &:hover { border-color: #10b981; }
    }
    .avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 100px; height: 100px; line-height: 100px; text-align: center; }
    .avatar { width: 100px; height: 100px; display: block; object-fit: cover; }
    .upload-tip { margin-top: 10px; font-size: 12px; color: #9ca3af; text-align: center; }
  }
}

/* 2. 订单卡片 */
.order-list {
  display: flex; flex-direction: column; gap: 16px;
  .order-card {
    border: 1px solid #f3f4f6; border-radius: 12px; overflow: hidden; background: #fff; transition: all 0.2s;
    &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.05); transform: translateY(-1px); }
    
    .card-header {
      background: #f9fafb; padding: 12px 20px; display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: #6b7280;
      .status-badge { font-weight: 600; &.status-danger {color:#ef4444} &.status-warning {color:#f59e0b} &.status-success {color:#10b981} &.status-primary {color:#3b82f6} }
    }
    .card-body {
      padding: 20px; display: flex; justify-content: space-between; align-items: center;
      .info-row {
        .price-box { margin-bottom: 8px; .label {font-size:12px; color:#6b7280; margin-right:8px} .value {font-size:18px; font-weight:bold; color:#1f2937; &.success{color:#10b981}} }
        .time-box, .address-box { font-size: 13px; color: #9ca3af; display: flex; align-items: center; gap: 4px; }
      }
      .action-row { display: flex; gap: 12px; }
    }
  }
}

/* 3. 商品网格 */
.product-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px;
  .manage-product-card {
    border: 1px solid #f3f4f6; border-radius: 12px; overflow: hidden; background: #fff; transition: all 0.2s;
    &:hover { box-shadow: 0 8px 20px rgba(0,0,0,0.06); transform: translateY(-2px); }
    
    .image-area {
      position: relative; height: 160px; overflow: hidden; cursor: pointer;
      .el-image { width: 100%; height: 100%; }
      .status-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.5); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: bold; }
    }
    .info-area {
      padding: 12px;
      .title { font-size: 14px; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #374151; }
      .price { color: #ef4444; font-weight: bold; font-size: 16px; margin-bottom: 12px; }
      .actions { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #f9fafb; padding-top: 8px; }
    }
  }
}

/* 4. 地址卡片网格 */
.address-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px;
  
  .address-card {
    border: 1px solid #f3f4f6; border-radius: 12px; padding: 20px; background: #fff; transition: all 0.2s; position: relative;
    &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.05); border-color: #d1fae5; }
    
    &.add-card {
      display: flex; flex-direction: column; align-items: center; justify-content: center; color: #9ca3af; cursor: pointer; border-style: dashed;
      .icon { font-size: 32px; margin-bottom: 8px; }
      &:hover { color: #10b981; border-color: #10b981; }
    }
    
    .addr-header { display: flex; justify-content: space-between; margin-bottom: 12px; font-weight: 600; color: #1f2937; }
    .addr-body { font-size: 13px; color: #6b7280; line-height: 1.5; min-height: 40px; margin-bottom: 16px; }
    .addr-actions { display: flex; justify-content: space-between; align-items: center; }
  }
}

/* 动画 */
.fade-down-enter-active, .fade-up-enter-active { transition: all 0.6s cubic-bezier(0.2, 0.8, 0.2, 1); }
.fade-down-enter-from { opacity: 0; transform: translateY(-30px); }
.fade-up-enter-from { opacity: 0; transform: translateY(30px); }
/* --- 【新增】销售统计模块样式 --- */
.stats-wrapper {
  padding: 0 10px;

  /* 1. 顶部指标卡片 */
  .stats-summary-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    margin-bottom: 32px;

    .stat-card {
      position: relative;
      padding: 24px;
      border-radius: 16px;
      color: #fff;
      overflow: hidden;
      box-shadow: 0 10px 20px rgba(0,0,0,0.05);
      transition: transform 0.3s;

      &:hover { transform: translateY(-4px); }

      /* 背景渐变 */
      &.gradient-blue { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); }
      &.gradient-green { background: linear-gradient(135deg, #10b981 0%, #059669 100%); }
      &.gradient-purple { background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%); }

      .icon-bg {
        position: absolute; right: -10px; top: -10px; opacity: 0.15; font-size: 100px; transform: rotate(15deg);
      }

      .content {
        position: relative; z-index: 2;
        .label { font-size: 14px; opacity: 0.9; margin-bottom: 8px; }
        .value { font-size: 28px; font-weight: 800; margin-bottom: 12px; }
      }
    }
  }

  /* 2. 图表区域 */
  .charts-grid {
    display: grid;
    grid-template-columns: 2fr 1fr; // 左侧趋势图宽，右侧饼图窄
    gap: 24px;

    .chart-card {
      background: #fff;
      border-radius: 16px;
      padding: 20px;
      border: 1px solid #f3f4f6;
      box-shadow: 0 4px 12px rgba(0,0,0,0.02);

      .chart-header {
        margin-bottom: 20px;
        .title { font-size: 16px; font-weight: 700; color: #374151; border-left: 4px solid #10b981; padding-left: 12px; }
      }

      .echart-box {
        width: 100%;
        height: 320px; // 固定高度确保渲染
      }
    }
  }
}

/* 响应式适配：小屏幕下改为单列 */
@media (max-width: 768px) {
  .stats-wrapper {
    .stats-summary-grid { grid-template-columns: 1fr; }
    .charts-grid { grid-template-columns: 1fr; }
  }
}

</style>