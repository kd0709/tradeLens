<template>
  <div class="product-detail-page">
    <div class="bg-decoration left"></div>
    <div class="bg-decoration right"></div>

    <div class="container" v-loading="loading">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>商品详情</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product?.title || '加载中...' }}</el-breadcrumb-item>
      </el-breadcrumb>

      <Transition name="fade-slide" mode="out-in">
        <div class="main-content" v-if="product" key="content">
          
          <div class="gallery-section">
            <div class="main-image">
              <el-image 
                :src="getFullImageUrl(currentImage)" 
                :preview-src-list="product.images.map(img => getFullImageUrl(img))"
                fit="cover"
                class="img-display"
                :initial-index="0"
              />
            </div>
            <div class="thumbnail-list">
              <div 
                v-for="(img, index) in product.images" 
                :key="index"
                :class="['thumb-item', { active: currentImage === img }]"
                @mouseenter="currentImage = img"
              >
                <img :src="getFullImageUrl(img)" alt="缩略图" />
              </div>
            </div>
          </div>

          <div class="info-section">
            <h1 class="product-title">{{ product.title }}</h1>
            
            <div class="price-card">
              <div class="price-row">
                <span class="currency">¥</span>
                <span class="amount">{{ product.price }}</span>
                <el-tag effect="dark" type="success" class="condition-tag">
                  {{ getConditionText(product.conditionLevel) }}
                </el-tag>
              </div>
              <div class="stock-row">
                <el-tag :type="product.quantity > 5 ? 'success' : product.quantity > 0 ? 'warning' : 'danger'">
                  库存: {{ product.quantity }}
                </el-tag>
              </div>
              <div class="meta-row">
                <span><el-icon><View /></el-icon> {{ product.viewCount }} 人围观</span>
                <el-divider direction="vertical" />
                <span>发布于 {{ formatDate(product.createTime) }}</span>
              </div>
            </div>

            <div class="seller-card">
              <el-avatar :size="50" :src="getFullImageUrl(product.sellerAvatar)" />
              <div class="seller-info">
                <div class="name">{{ product.sellerNickname || '匿名卖家' }}</div>
                <div class="desc">信用极好 · 回复快</div>
              </div>
              <el-button round size="small" @click="handleChat">私 聊</el-button>
            </div>

            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="large" 
                class="action-btn buy-btn"
                @click="handleBuy"
              >
                立即购买
              </el-button>
              
              <el-button 
                size="large" 
                class="action-btn cart-btn" 
                icon="ShoppingCart"
                @click="handleAddToCart"
              >
                加入购物车
              </el-button>

              <el-button 
                size="large" 
                class="action-btn fav-btn"
                :type="isLiked ? 'warning' : 'default'"
                :plain="isLiked"
                icon="Star" 
                @click="toggleLike"
              >
                {{ isLiked ? '已收藏' : '收藏' }}
              </el-button>
            </div>

            <div class="description-box">
              <h3>商品描述</h3>
              <div class="desc-content">
                <p class="desc-text">{{ product.description || '卖家很懒，没有填写描述...' }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <el-empty v-else-if="!loading" description="商品不存在或已下架" key="empty" />
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Star, ShoppingCart } from '@element-plus/icons-vue'
import { showSuccess, showError, showWarning, showInfo } from '@/utils/message'

import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import { toggleFavorite } from '@/api/favorite'
import type { ProductDto } from '@/dto/product'
import { getFullImageUrl } from '@/utils/image'
import type { FavoriteToggleDto } from '@/dto/favorite'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const product = ref<ProductDto | null>(null)
const currentImage = ref('')
const isLiked = ref(false)


const getConditionText = (level: number) => {
  const map: Record<number, string> = { 1: '全新', 2: '99新', 3: '9成新', 4: '8成新' }
  return map[level] || '二手'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.split('T')[0]
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    const res = await getProductDetail(id)
    product.value = res as ProductDto
    isLiked.value = (product.value as any).isFavorited || false
    if (product.value?.images && product.value.images.length > 0) {
      currentImage.value = getFullImageUrl(product.value.images[0])
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

const handleBuy = () => {
  if (!product.value) return
  
  if (product.value.quantity <= 0) {
    showWarning('抱歉，该商品库存不足，无法购买')
    return
  }
  
  router.push({ 
    path: '/order/create', 
    query: { productId: product.value.id } 
  })
}

const handleAddToCart = async () => {
  if (!product.value) return
  
  if (product.value.quantity <= 0) {
    showWarning('抱歉，该商品库存不足，无法加入购物车')
    return
  }
  
  const loadingMsg = ElMessage({
    message: '正在添加到购物车...',
    duration: 0,
    icon: 'Loading'
  })
  
  try {
    await addToCart({ productId: product.value.id, quantity: 1 })
    loadingMsg.close()
    showSuccess('成功加入购物车！', 2000)
    
    const btn = document.querySelector('.cart-btn')
    if (btn) {
      btn.classList.add('cart-btn-animate')
      setTimeout(() => {
        btn.classList.remove('cart-btn-animate')
      }, 600)
    }
  } catch (e: any) {
    loadingMsg.close()
    showError(e.message || '加入购物车失败', 3000)
  }
}

const toggleLike = async () => {
  if (!product.value) return
  try { 
    isLiked.value = !isLiked.value
    
    const toggleDto: FavoriteToggleDto = { productId: product.value.id }
    const serverStatus = await toggleFavorite(toggleDto)
    
    if (serverStatus !== isLiked.value) {
        isLiked.value = serverStatus
    }
    
    showSuccess(serverStatus ? '已添加至收藏夹' : '已取消收藏')
  } catch (error) {
    isLiked.value = !isLiked.value 
    showError('操作失败')
  }
}

const handleChat = () => {
  if (!product.value) return

  if (!authStore.isLoggedIn) {
    showWarning('请先登录后再联系卖家')
    router.push('/login')
    return
  }
  
  // 【核心修复】：全面兼容 sellerId 和 userId，并强制转换为数字
  const targetUserId = Number(product.value.sellerId || product.value.userId)

  // 拦截空值：如果走到这里报错，说明后端商品详情接口根本没有返回卖家的ID！
  if (!targetUserId || isNaN(targetUserId) || targetUserId <= 0) {
    showError('无法获取卖家ID，请检查后端详情接口是否返回了 sellerId 或 userId')
    return
  }

  if (authStore.user?.id === targetUserId) {
    showInfo('不能和自己聊天哦')
    return
  }

  router.push({ 
    path: '/messages', 
    query: { 
      targetId: targetUserId,
      nickname: product.value.sellerNickname || '匿名卖家',
      avatar: product.value.sellerAvatar || ''
    } 
  })
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.product-detail-page {
  position: relative;
  min-height: calc(100vh - 64px);
  padding-bottom: 60px;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  overflow: hidden;

  /* 背景几何装饰 */
  .bg-decoration {
    position: absolute;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.5;
    z-index: 0;
    
    &.left {
      background: #d1fae5; // 浅绿
      top: -100px;
      left: -100px;
    }
    &.right {
      background: #e0e7ff; // 浅蓝
      bottom: -100px;
      right: -100px;
    }
  }
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px;
  position: relative;
  z-index: 1;
}

.breadcrumb {
  margin-bottom: 24px;
  font-size: 14px;
}

.main-content {
  display: grid;
  grid-template-columns: 480px 1fr;
  gap: 48px;
  align-items: start;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  padding: 32px;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

/* 左侧图片画廊 */
.gallery-section {
  .main-image {
    width: 100%;
    height: 480px;
    border-radius: 12px;
    overflow: hidden;
    border: 1px solid #f3f4f6;
    margin-bottom: 16px;
    background: #fff;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    
    .img-display {
      width: 100%;
      height: 100%;
      cursor: zoom-in;
    }
  }

  .thumbnail-list {
    display: flex;
    gap: 12px;
    overflow-x: auto;
    padding: 4px 2px;
    
    &::-webkit-scrollbar { height: 4px; }
    &::-webkit-scrollbar-thumb { background: #e5e7eb; border-radius: 2px; }

    .thumb-item {
      width: 70px;
      height: 70px;
      border-radius: 8px;
      overflow: hidden;
      border: 2px solid transparent;
      cursor: pointer;
      opacity: 0.6;
      transition: all 0.2s;
      flex-shrink: 0;
      
      &.active {
        border-color: #10b981;
        opacity: 1;
        transform: translateY(-2px);
        box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.2);
      }
      
      &:hover { opacity: 1; }
      
      img { width: 100%; height: 100%; object-fit: cover; }
    }
  }
}

/* 右侧信息区 */
.info-section {
  .product-title {
    font-size: 28px;
    font-weight: 700;
    color: #111827;
    margin: 0 0 24px;
    line-height: 1.3;
  }

  .price-card {
    background: linear-gradient(to right, #fef2f2, #fff1f2); // 浅红背景强调价格
    padding: 24px;
    border-radius: 16px;
    margin-bottom: 24px;
    border: 1px solid #fee2e2;
    
    .price-row {
      display: flex;
      align-items: baseline;
      gap: 4px;
      margin-bottom: 12px;
      
      .currency { font-size: 20px; color: #ef4444; font-weight: bold; }
      .amount { font-size: 40px; color: #ef4444; font-weight: 800; letter-spacing: -1px; }
      .condition-tag { margin-left: 16px; transform: translateY(-8px); }
    }
    
    .stock-row {
      margin-bottom: 12px;
      
      .el-tag {
        font-size: 14px;
        padding: 6px 12px;
      }
    }
    
    .meta-row {
      display: flex;
      align-items: center;
      font-size: 13px;
      color: #6b7280;
      gap: 8px;
      .el-icon { vertical-align: middle; margin-right: 4px; }
    }
  }

  .seller-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 20px;
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    margin-bottom: 32px;
    transition: all 0.2s;

    &:hover { border-color: #d1d5db; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
    
    .seller-info {
      flex: 1;
      .name { font-weight: 600; color: #374151; font-size: 15px; }
      .desc { font-size: 12px; color: #6b7280; margin-top: 4px; }
    }
  }

  .action-buttons {
    display: flex;
    gap: 16px;
    margin-bottom: 40px;
    
    .action-btn {
      height: 50px;
      border-radius: 25px; // 胶囊圆角
      font-size: 16px;
      font-weight: 600;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
      &:active { transform: translateY(0); }
    }

    .buy-btn {
      flex: 2;
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
    }
    
    .cart-btn {
      flex: 1.5;
    }

    .fav-btn {
      flex: 1;
      min-width: 100px;
    }
  }

  .description-box {
    h3 {
      font-size: 18px;
      font-weight: 700;
      margin-bottom: 16px;
      position: relative;
      padding-left: 16px;
      color: #374151;
      
      &::before {
        content: '';
        position: absolute;
        left: 0; top: 4px; bottom: 4px; width: 4px;
        background: #10b981;
        border-radius: 2px;
      }
    }
    
    .desc-content {
      background: #f9fafb;
      padding: 24px;
      border-radius: 12px;
      
      .desc-text {
        font-size: 15px;
        line-height: 1.8;
        color: #4b5563;
        white-space: pre-wrap; 
        margin: 0;
      }
    }
  }
}

/*过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/*车按钮动画 */
.cart-btn-animate {
  animation: cartPulse 0.6s ease-in-out;
}

@keyframes cartPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4); }
  100% { transform: scale(1); }
}

/*心动画 */
.btn-heart-beat {
  animation: heartBeat 0.6s ease-in-out;
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  14% { transform: scale(1.1); }
  28% { transform: scale(1); }
  42% { transform: scale(1.1); }
  70% { transform: scale(1); }
}

/*点击效果动画 */
.btn-click-effect {
  animation: clickEffect 0.3s ease-out;
}

@keyframes clickEffect {
  0% { transform: scale(1); }
  50% { transform: scale(0.95); }
  100% { transform: scale(1); }
}

/* 响应式适配 */
@media (max-width: 900px) {
  .main-content {
    grid-template-columns: 1fr;
    gap: 32px;
  }
  
  .gallery-section .main-image {
    height: 400px;
  }
  
  .action-buttons {
    flex-wrap: wrap;
    .buy-btn { width: 100%; flex: auto; order: 1; }
    .cart-btn { width: calc(50% - 8px); flex: auto; order: 2; }
    .fav-btn { width: calc(50% - 8px); flex: auto; order: 3; }
  }
}
</style>