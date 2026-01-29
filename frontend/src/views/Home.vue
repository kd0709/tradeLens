<template>
  <div class="home-page">
    <div class="filter-section">
      <div class="container">
        <span 
          v-for="cat in categories" 
          :key="cat.id" 
          :class="['category-tag', { active: currentCategory === cat.id }]"
          @click="selectCategory(cat.id)"
        >
          {{ cat.name }}
        </span>
      </div>
    </div>

    <div class="container product-grid">
      <div 
        v-for="item in productList" 
        :key="item.id" 
        class="product-card"
        @click="goToDetail(item.id)"
      >
        <div class="image-wrapper">
          <img :src="item.images[0]" :alt="item.title" loading="lazy" />
          <span class="condition-tag">{{ getConditionText(item.conditionLevel) }}</span>
        </div>
        <div class="info">
          <h3 class="title">{{ item.title }}</h3>
          <div class="price-row">
            <span class="currency">¥</span>
            <span class="price">{{ item.price }}</span>
            <span class="view-count">{{ item.viewCount }}人看过</span>
          </div>
          <div class="seller-row">
            <el-avatar :size="20" :src="item.seller?.avatar" />
            <span class="name">{{ item.seller?.nickname }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="loading-state" v-if="loading">
      <el-skeleton :rows="3" animated />
    </div>
    <div class="empty-state" v-if="!loading && productList.length === 0">
      <el-empty description="暂无相关商品" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProductList } from '@/api/product'
import type { ProductDto } from '@/dto/product'

const router = useRouter()
const loading = ref(false)
const productList = ref<ProductDto[]>([])
const currentCategory = ref(0)

// 模拟分类数据（后端接口 ready 后可替换）
const categories = [
  { id: 0, name: '全部' },
  { id: 1, name: '数码产品' },
  { id: 2, name: '书籍教材' },
  { id: 3, name: '生活用品' },
  { id: 4, name: '美妆护肤' },
]

// 获取成色文本
const getConditionText = (level: number) => {
  const map: Record<number, string> = { 1: '全新', 2: '99新', 3: '9成新', 4: '8成新' }
  return map[level] || '二手'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductList({
      page: 1,
      size: 20,
      categoryId: currentCategory.value || undefined
    })
    // 假设后端返回结构 { code: 200, data: { records: [] } }
    // 如果没有真实后端，这里需要造一些 Mock 数据防止页面空白
    productList.value = res.records || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const selectCategory = (id: number) => {
  currentCategory.value = id
  loadData()
}

const goToDetail = (id: number) => {
  router.push(`/product/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.filter-section {
  background: #fff;
  padding: 16px 0;
  margin-bottom: 24px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  
  .category-tag {
    display: inline-block;
    padding: 6px 16px;
    margin-right: 12px;
    border-radius: 20px;
    font-size: 14px;
    color: #4b5563;
    cursor: pointer;
    transition: all 0.2s;
    background: #f3f4f6;
    
    &:hover { background: #e5e7eb; }
    
    &.active {
      background: #10b981;
      color: #fff;
      font-weight: 600;
    }
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
  padding-bottom: 40px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f3f4f6;
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  }

  .image-wrapper {
    position: relative;
    aspect-ratio: 1; /* 正方形封面 */
    background: #f9fafb;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .condition-tag {
      position: absolute;
      top: 8px;
      left: 8px;
      background: rgba(0, 0, 0, 0.6);
      color: #fff;
      font-size: 12px;
      padding: 2px 6px;
      border-radius: 4px;
      backdrop-filter: blur(4px);
    }
  }

  .info {
    padding: 12px;
    
    .title {
      font-size: 15px;
      color: #111827;
      margin: 0 0 8px;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      font-weight: 500;
    }
    
    .price-row {
      display: flex;
      align-items: baseline;
      margin-bottom: 8px;
      
      .currency { font-size: 12px; color: #ef4444; font-weight: bold; }
      .price { font-size: 18px; color: #ef4444; font-weight: bold; margin-right: auto; }
      .view-count { font-size: 12px; color: #9ca3af; }
    }
    
    .seller-row {
      display: flex;
      align-items: center;
      gap: 6px;
      padding-top: 8px;
      border-top: 1px solid #f3f4f6;
      
      .name { font-size: 12px; color: #6b7280; }
    }
  }
}

.loading-state, .empty-state {
  padding: 40px 0;
  text-align: center;
}
</style>