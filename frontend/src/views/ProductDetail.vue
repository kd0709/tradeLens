<template>
  <div class="product-detail-page">
    <div class="container" v-loading="loading">
      
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>商品详情</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product?.title || '加载中...' }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="main-content" v-if="product">
        <div class="gallery-section">
          <div class="main-image">
            <el-image 
              :src="getFullImageUrl(currentImage)" 
              :preview-src-list="product.images.map(img => getFullImageUrl(img))"
              fit="cover"
              class="img-display"
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
              <el-tag effect="plain" type="success" class="condition-tag">
                {{ getConditionText(product.conditionLevel) }}
              </el-tag>
            </div>
            <div class="meta-row">
              <span>浏览 {{ product.viewCount }}</span>
              <el-divider direction="vertical" />
              <span>发布于 {{ formatDate(product.createTime) }}</span>
            </div>
          </div>

          <div class="seller-card">
            <el-avatar :size="48" :src="getFullImageUrl(product.seller?.avatar)" />
            <div class="seller-info">
              <div class="name">{{ product.seller?.nickname || '匿名卖家' }}</div>
              <div class="desc">信用极好 · 回复快</div>
            </div>
            <el-button round size="small">私聊</el-button>
          </div>

          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="large" 
              class="buy-btn"
              @click="handleBuy"
            >
              立即购买
            </el-button>
            <el-button 
              size="large" 
              class="cart-btn" 
              icon="ShoppingCart"
              @click="handleAddToCart"
            >
              加入购物车
            </el-button>
            <el-button 
              size="large" 
              circle 
              icon="Star" 
              :type="isLiked ? 'warning' : ''"
              @click="toggleLike"
            />
          </div>

          <div class="description-box">
            <h3>商品描述</h3>
            <p class="desc-text">{{ product.description || '卖家很懒，没有填写描述...' }}</p>
          </div>
        </div>
      </div>
      
      <el-empty v-else-if="!loading" description="商品不存在或已下架" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import { toggleFavorite } from '@/api/favorite'
import type { ProductDto } from '@/dto/product'
import { getFullImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const product = ref<ProductDto | null>(null)
const currentImage = ref('')
const isLiked = ref(false)

// 获取成色文本
const getConditionText = (level: number) => {
  const map: Record<number, string> = { 1: '全新', 2: '99新', 3: '9成新', 4: '8成新' }
  return map[level] || '二手'
}

// 格式化日期 (简单版)
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.split('T')[0]
}

// 加载详情
const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    const res = await getProductDetail(id)
    product.value = res as ProductDto
    
    // 设置收藏状态（后端返回isFavorited字段）
    isLiked.value = (product.value as any).isFavorited || false
    
    // 初始化主图
    if (product.value?.images && product.value.images.length > 0) {
      currentImage.value = getFullImageUrl(product.value.images[0])
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// 购买逻辑
const handleBuy = () => {
  if (!product.value) return
  // 跳转到下单页，带上 productId
  router.push({ 
    path: '/order/create', 
    query: { productId: product.value.id } 
  })
}

// 加入购物车
const handleAddToCart = async () => {
  if (!product.value) return
  try {
    await addToCart({ productId: product.value.id, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (e) {
    console.error(e)
  }
}

const toggleLike = async () => {
  if (!product.value) return
  try {
    const newStatus = await toggleFavorite(product.value.id)
    isLiked.value = newStatus
    ElMessage.success(newStatus ? '收藏成功' : '已取消收藏')
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.product-detail-page {
  background-color: #fff;
  min-height: calc(100vh - 64px);
  padding-bottom: 40px;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 24px;
  font-size: 14px;
}

.main-content {
  display: grid;
  grid-template-columns: 500px 1fr;
  gap: 40px;
  align-items: start;
}

/* 左侧图片画廊 */
.gallery-section {
  .main-image {
    width: 100%;
    height: 500px;
    border-radius: 12px;
    overflow: hidden;
    border: 1px solid #f3f4f6;
    margin-bottom: 16px;
    background: #f9fafb;
    
    .img-display {
      width: 100%;
      height: 100%;
    }
  }

  .thumbnail-list {
    display: flex;
    gap: 12px;
    overflow-x: auto;
    padding-bottom: 4px;
    
    .thumb-item {
      width: 80px;
      height: 80px;
      border-radius: 8px;
      overflow: hidden;
      border: 2px solid transparent;
      cursor: pointer;
      opacity: 0.7;
      transition: all 0.2s;
      
      &.active {
        border-color: #10b981;
        opacity: 1;
      }
      
      &:hover { opacity: 1; }
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }
}

/* 右侧信息区 */
.info-section {
  .product-title {
    font-size: 24px;
    font-weight: 600;
    color: #111827;
    margin: 0 0 20px;
    line-height: 1.4;
  }

  .price-card {
    background: #f9fafb;
    padding: 20px;
    border-radius: 12px;
    margin-bottom: 24px;
    
    .price-row {
      display: flex;
      align-items: baseline;
      gap: 4px;
      margin-bottom: 8px;
      
      .currency { font-size: 18px; color: #ef4444; font-weight: bold; }
      .amount { font-size: 32px; color: #ef4444; font-weight: bold; }
      .condition-tag { margin-left: 12px; }
    }
    
    .meta-row {
      font-size: 13px;
      color: #9ca3af;
    }
  }

  .seller-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    margin-bottom: 30px;
    
    .seller-info {
      flex: 1;
      .name { font-weight: 600; color: #374151; }
      .desc { font-size: 12px; color: #6b7280; margin-top: 2px; }
    }
  }

  .action-buttons {
    display: flex;
    gap: 16px;
    margin-bottom: 40px;
    
    .buy-btn {
      flex: 2;
      background-color: #10b981;
      border-color: #10b981;
      font-weight: 600;
      
      &:hover { background-color: #059669; border-color: #059669; }
    }
    
    .cart-btn {
      flex: 1;
    }
  }

  .description-box {
    h3 {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 12px;
      position: relative;
      padding-left: 12px;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 4px;
        bottom: 4px;
        width: 4px;
        background: #10b981;
        border-radius: 2px;
      }
    }
    
    .desc-text {
      font-size: 15px;
      line-height: 1.8;
      color: #4b5563;
      white-space: pre-line; /* 保留换行符 */
    }
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .gallery-section .main-image {
    height: 350px;
  }
}
</style>