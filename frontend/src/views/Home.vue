<template>
  <div class="home-page">
    <div class="banner-section" v-if="banners.length > 0">
      <div class="container">
        <el-carousel :interval="4000" type="card" height="300px">
          <el-carousel-item v-for="(item, index) in banners" :key="index">
            <div class="banner-item" :style="{ backgroundImage: `url(${item.img})` }">
              <div class="banner-content">
                <h3>{{ item.title }}</h3>
                <p>{{ item.desc }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <div class="toolbar-section" :class="{ sticky: isSticky }">
      <div class="container toolbar-content">
        <div class="category-list">
          <span 
            v-for="cat in categories" 
            :key="cat.id" 
            :class="['cat-item', { active: (queryParams.categoryId === cat.id) || (cat.id === 0 && !queryParams.categoryId) }]"
            @click="handleCategoryChange(cat.id)"
          >
            {{ cat.name }}
          </span>
        </div>

        <div class="sort-list">
          <span 
            :class="['sort-item', { active: !queryParams.sort }]" 
            @click="handleSortChange(undefined)"
          >综合</span>
          <span 
            :class="['sort-item', { active: queryParams.sort === 'time_desc' }]" 
            @click="handleSortChange('time_desc')"
          >最新</span>
          <span 
            class="sort-item price-sort"
            :class="{ active: queryParams.sort?.startsWith('price') }"
            @click="togglePriceSort"
          >
            价格
            <el-icon class="icon">
              <CaretTop v-if="queryParams.sort === 'price_asc'" />
              <CaretBottom v-else-if="queryParams.sort === 'price_desc'" />
              <d-caret v-else /> 
            </el-icon>
          </span>
        </div>
      </div>
    </div>

    <div class="container product-grid">
      <div 
        v-for="item in productList" 
        :key="item.id" 
        class="product-card"
        @click="goToDetail(item)"
      >
        <div class="image-wrapper">
          <img 
            :src="getFullImageUrl(item.mainImage)" 
            :alt="item.title" 
            loading="lazy" 
          />
          <div class="condition-tag" v-if="item.conditionLevel">
            {{ getConditionText(item.conditionLevel) }}
          </div>
        </div>
        <div class="info">
          <h3 class="title" v-html="highlightKeyword(item.title)"></h3>
          <div class="price-row">
            <span class="currency">¥</span>
            <span class="price">{{ item.price }}</span>
            <span class="view-count">{{ item.viewCount }}人看过</span>
          </div>
          <div class="seller-row">
            <el-avatar :size="20" :src="getFullImageUrl(item.sellerAvatar) || defaultAvatar" />
            <span class="name">{{ item.sellerNickname || '匿名' }}</span>
            <span class="time">{{ formatTime(item.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="container loading-section">
      <el-skeleton v-if="loading" :rows="3" animated />
      <el-empty v-if="!loading && productList.length === 0" description="暂无相关商品" />
      <div v-if="!loading && productList.length > 0" class="no-more">
        - 到底啦，快去发布闲置吧 -
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { CaretTop, CaretBottom, DCaret } from '@element-plus/icons-vue'

// API 接口
import { getProductList } from '@/api/product'
import { getCategoryList } from '@/api/category'

// DTO 导入
import { getFullImageUrl } from '@/utils/image' 
import type { ProductQuery } from '@/dto/product'

const router = useRouter()
const route = useRoute()

// 状态定义
const loading = ref(false)
const productList = ref<any[]>([]) // 直接使用后端返回的列表对象
const categories = ref<Array<{ id: number; name: string }>>([{ id: 0, name: '全部' }])
const isSticky = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const banners = ref<any[]>([]) // 模拟数据已删除，预留接口位置

// 查询参数
const queryParams = reactive<ProductQuery>({
  current: 1,
  size: 20,
  status: 2,
  categoryId: undefined,
  keyword: '',
  sort: undefined
})



// 加载分类数据
const loadCategories = async () => {
  try {
    const catList = await getCategoryList()
    categories.value = [
      { id: 0, name: '全部' },
      ...catList.map((cat: any) => ({ id: cat.id, name: cat.name }))
    ]
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载商品数据
const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      current: queryParams.current,
      size: queryParams.size,
      keyword: queryParams.keyword || undefined,
      sort: queryParams.sort
    }
    if (queryParams.categoryId && queryParams.categoryId !== 0) {
      params.categoryId = queryParams.categoryId
    }
    
    const res = await getProductList(params)
    productList.value = res.list || [] 
    console.log('加载商品列表成功:', productList.value)
  } catch (error) {
    console.error('加载商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// --- 交互处理 ---

const handleCategoryChange = (id: number) => {
  queryParams.categoryId = id === 0 ? undefined : id
  queryParams.current = 1
  loadData()
}

const handleSortChange = (sortType: any) => {
  queryParams.sort = sortType
  loadData()
}

const togglePriceSort = () => {
  queryParams.sort = queryParams.sort === 'price_asc' ? 'price_desc' : 'price_asc'
  loadData()
}

const goToDetail = (item: any) => {
  router.push(`/product/${item.id}`)
}

// --- 辅助工具 ---

const getConditionText = (level: number) => {
  return { 1: '全新', 2: '99新', 3: '9成新', 4: '8成新' }[level] || '二手'
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  return timeStr.split('T')[0]
}

const highlightKeyword = (title: string) => {
  if (!queryParams.keyword) return title
  const reg = new RegExp(queryParams.keyword, 'gi')
  return title.replace(reg, (match) => `<span style="color: #10b981; font-weight:bold">${match}</span>`)
}

// 监听搜索词变化
watch(() => route.query.q, (newVal) => {
  queryParams.keyword = (newVal as string) || ''
  loadData()
})

onMounted(async () => {
  if (route.query.q) {
    queryParams.keyword = route.query.q as string
  }
  await loadCategories()
  loadData()
})
</script>

<style scoped lang="scss">
.home-page {
  background-color: #f9fafb;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 1. Banner 样式 */
.banner-section {
  padding-top: 24px;
  margin-bottom: 24px;
  
  .banner-item {
    height: 100%;
    border-radius: 12px;
    background-size: cover;
    background-position: center;
    position: relative;
    overflow: hidden;
    
    /* 渐变遮罩 */
    &::after {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
    }
    
    .banner-content {
      position: absolute;
      bottom: 40px;
      left: 40px;
      z-index: 2;
      color: #fff;
      text-shadow: 0 2px 4px rgba(0,0,0,0.3);
      
      h3 { font-size: 28px; margin: 0 0 8px; font-weight: 700; }
      p { font-size: 16px; margin: 0; opacity: 0.9; }
    }
  }
}

/* 2. 工具栏样式 */
.toolbar-section {
  position: sticky;
  top: 64px; /* NavBar 高度 */
  z-index: 10;
  background: rgba(249, 250, 251, 0.95);
  backdrop-filter: blur(10px);
  padding: 16px 0;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  margin-bottom: 24px;
  transition: all 0.3s;
  
  .toolbar-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .category-list {
    display: flex;
    gap: 12px;
    overflow-x: auto;
    
    .cat-item {
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 14px;
      color: #4b5563;
      background: #fff;
      border: 1px solid #e5e7eb;
      cursor: pointer;
      transition: all 0.2s;
      white-space: nowrap;
      
      &:hover { border-color: #10b981; color: #10b981; }
      &.active { background: #10b981; color: #fff; border-color: #10b981; }
    }
  }
  
  .sort-list {
    display: flex;
    gap: 24px;
    font-size: 14px;
    color: #6b7280;
    
    .sort-item {
      cursor: pointer;
      &:hover { color: #10b981; }
      &.active { color: #10b981; font-weight: 600; }
      
      &.price-sort {
        display: flex;
        align-items: center;
        gap: 2px;
        .icon { font-size: 12px; }
      }
    }
  }
}

/* 3. 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 24px;
  padding-bottom: 40px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid transparent;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 20px -5px rgba(0, 0, 0, 0.1);
    border-color: #e5e7eb;
  }

  .image-wrapper {
    position: relative;
    aspect-ratio: 1;
    background: #f3f4f6;
    
    img { width: 100%; height: 100%; object-fit: cover; }
    .condition-tag {
      position: absolute; top: 8px; left: 8px;
      background: rgba(0, 0, 0, 0.6); color: #fff;
      font-size: 12px; padding: 2px 6px; border-radius: 4px;
      backdrop-filter: blur(4px);
    }
  }

  .info {
    padding: 12px;
    
    .title {
      font-size: 15px; color: #1f2937; margin: 0 0 8px;
      line-height: 1.4; height: 42px; /* 限制2行高度 */
      display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
    }
    
    .price-row {
      display: flex; align-items: baseline; margin-bottom: 8px;
      .currency { font-size: 12px; color: #ef4444; font-weight: bold; }
      .price { font-size: 18px; color: #ef4444; font-weight: bold; margin-right: auto; }
      .view-count { font-size: 12px; color: #9ca3af; }
    }
    
    .seller-row {
      display: flex; align-items: center; gap: 6px;
      padding-top: 8px; border-top: 1px solid #f9fafb;
      .name { font-size: 12px; color: #6b7280; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .time { font-size: 12px; color: #d1d5db; }
    }
  }
}

.loading-section {
  text-align: center;
  padding: 40px 0;
  .no-more { color: #d1d5db; font-size: 13px; margin-top: 20px; }
}
</style>