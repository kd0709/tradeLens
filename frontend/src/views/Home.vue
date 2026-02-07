<template>
  <div class="home-page">
    <div class="banner-section-wrapper">
      <div class="banner-bg-decoration left"></div>
      <div class="banner-bg-decoration right"></div>
      
      <div class="container banner-container">
        <el-carousel :interval="5000" type="card" height="320px" indicator-position="outside">
          <el-carousel-item v-for="(item, index) in banners" :key="index">
            <div class="banner-item" :style="{ backgroundImage: `url(${item.img})` }">
              <div class="banner-mask"></div>
              <div class="banner-content">
                <span class="banner-tag">{{ item.tag }}</span>
                <h3>{{ item.title }}</h3>
                <p>{{ item.desc }}</p>
                <el-button type="primary" round size="small" class="banner-btn">立即查看</el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <div class="toolbar-section" :class="{ sticky: isSticky }">
      <div class="container toolbar-content">
        <div class="category-scroll-wrapper">
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
          <div class="scroll-mask"></div>
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

    <div class="container product-grid" v-loading="loading" element-loading-text="加载中..." element-loading-background="rgba(255, 255, 255, 0.6)">
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

    <div class="container pagination-section" v-if="total > 0 && !loading">
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :page-sizes="[12, 20, 32, 48]"
        :background="true"
        layout="total, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="custom-pagination"
      />
    </div>

    <div class="container empty-section" v-if="!loading && productList.length === 0">
      <el-empty description="暂无相关商品，换个词试试？" />
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
const productList = ref<any[]>([]) 
const total = ref(0) // 新增：总条数
const categories = ref<Array<{ id: number; name: string }>>([{ id: 0, name: '全部' }])
const isSticky = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 模拟 Banner 数据
const banners = ref<any[]>([
  { 
    title: '数码发烧友', 
    desc: '低价淘大牌，旧机换新颜', 
    tag: '热门活动',
    img: 'https://picsum.photos/800/400?random=1' 
  },
  { 
    title: '校园开学季', 
    desc: '教材、生活用品一站式购齐', 
    tag: '限时特惠',
    img: 'https://picsum.photos/800/400?random=2' 
  },
  { 
    title: '极客装备库', 
    desc: '显卡、外设，硬核玩家的选择', 
    tag: '新品上架',
    img: 'https://picsum.photos/800/400?random=3' 
  },
  { 
    title: '闲置书屋', 
    desc: '让知识流动起来', 
    tag: '图书漂流',
    img: 'https://picsum.photos/800/400?random=4' 
  }
])

// 查询参数
const queryParams = reactive<ProductQuery>({
  current: 1,
  size: 12,
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
    // 更新总条数，兼容后端可能返回 null 的情况
    total.value = Number(res.total) || 0 
  } catch (error) {
    console.error('加载商品列表失败:', error)
    productList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// --- 交互处理 ---

const handleCategoryChange = (id: number) => {
  if (queryParams.categoryId === id) return
  
  queryParams.categoryId = id === 0 ? undefined : id
  queryParams.current = 1 // 切换分类重置为第一页
  loadData()
}

const handleSortChange = (sortType: any) => {
  if (queryParams.sort === sortType) return

  queryParams.sort = sortType
  queryParams.current = 1 // 切换排序重置为第一页
  loadData()
}

const togglePriceSort = () => {
  queryParams.sort = queryParams.sort === 'price_asc' ? 'price_desc' : 'price_asc'
  queryParams.current = 1
  loadData()
}

// 分页处理：改变每页条数
const handleSizeChange = (val: number) => {
  queryParams.size = val
  queryParams.current = 1 // 改变每页条数时重置为第一页
  loadData()
  scrollToProductArea()
}

// 分页处理：翻页
const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadData()
  scrollToProductArea()
}

// 辅助：滚动到商品区域顶部
const scrollToProductArea = () => {
  const toolbar = document.querySelector('.toolbar-section')
  if (toolbar) {
    // 减去工具栏高度和一点间距，保留视觉连贯性
    const top = toolbar.getBoundingClientRect().top + window.pageYOffset - 80
    window.scrollTo({ top: top > 0 ? top : 0, behavior: 'smooth' })
  } else {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const goToDetail = (item: any) => {
  router.push(`/product/${item.id}`)
}

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

watch(() => route.query.q, (newVal) => {
  queryParams.keyword = (newVal as string) || ''
  queryParams.current = 1 // 搜索时重置为第一页
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

/* 1. Banner 区域样式 (保持不变) */
.banner-section-wrapper {
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, #ecfdf5 0%, #f9fafb 100%);
  padding-top: 32px;
  margin-bottom: 24px;

  .banner-bg-decoration {
    position: absolute;
    width: 300px;
    height: 300px;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.6;
    z-index: 0;
    
    &.left { background: #10b981; top: -100px; left: -100px; }
    &.right { background: #34d399; bottom: -50px; right: -100px; }
  }

  .banner-container { position: relative; z-index: 1; }

  .banner-item {
    height: 100%;
    border-radius: 16px;
    background-size: cover;
    background-position: center;
    position: relative;
    overflow: hidden;
    box-shadow: 0 8px 20px rgba(0,0,0,0.1);
    
    .banner-mask {
      position: absolute;
      inset: 0;
      background: linear-gradient(to right, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.3) 50%, transparent 100%);
    }
    
    .banner-content {
      position: absolute; top: 50%; left: 48px; transform: translateY(-50%);
      z-index: 2; color: #fff; max-width: 50%;
      
      .banner-tag {
        display: inline-block; padding: 4px 10px;
        background: rgba(16, 185, 129, 0.9); border-radius: 20px;
        font-size: 12px; font-weight: 600; margin-bottom: 12px;
        backdrop-filter: blur(4px);
      }
      h3 { font-size: 32px; margin: 0 0 12px; font-weight: 800; letter-spacing: 1px; text-shadow: 0 2px 4px rgba(0,0,0,0.2); }
      p { font-size: 16px; margin: 0 0 24px; opacity: 0.9; line-height: 1.5; }
      .banner-btn {
        background: #fff; color: #059669; border: none; font-weight: 600;
        &:hover { background: #f0fdf4; transform: translateY(-2px); }
      }
    }
  }
}

/* 2. 工具栏样式 (保持不变) */
.toolbar-section {
  position: sticky; top: 64px; z-index: 10;
  background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(16px);
  padding: 16px 0;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02);
  transition: all 0.3s;
  
  .toolbar-content {
    display: flex; justify-content: space-between; align-items: center; gap: 24px;
  }
  
  .category-scroll-wrapper {
    flex: 1; position: relative; overflow: hidden;
    .category-list {
      display: flex; gap: 12px; overflow-x: auto; padding-bottom: 4px; padding-right: 20px;
      &::-webkit-scrollbar { display: none; }
      scrollbar-width: none;
      .cat-item {
        flex-shrink: 0; padding: 8px 18px; border-radius: 12px;
        font-size: 14px; font-weight: 500; color: #4b5563; background: #fff; border: 1px solid #e5e7eb;
        cursor: pointer; transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        &:hover { border-color: #10b981; color: #10b981; transform: translateY(-2px); box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.15); }
        &:active { transform: translateY(0); }
        &.active { background: #10b981; color: #fff; border-color: #10b981; box-shadow: 0 4px 10px -2px rgba(16, 185, 129, 0.4); }
      }
    }
    .scroll-mask {
      position: absolute; right: 0; top: 0; bottom: 0; width: 40px;
      background: linear-gradient(to right, transparent, rgba(255,255,255,0.9)); pointer-events: none;
    }
  }
  
  .sort-list {
    flex-shrink: 0; display: flex; gap: 16px; padding-left: 16px; border-left: 1px solid #f3f4f6;
    .sort-item {
      font-size: 14px; color: #6b7280; cursor: pointer; padding: 6px 12px; border-radius: 8px; transition: all 0.2s;
      &:hover, &.active { color: #10b981; background-color: #ecfdf5; }
      &.active { font-weight: 600; }
      &.price-sort { display: flex; align-items: center; gap: 4px; .icon { font-size: 12px; } }
    }
  }
}

/* 3. 商品网格样式 (保持不变) */
.product-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px; padding-bottom: 20px; min-height: 300px; // 防止加载时高度塌陷
}

.product-card {
  background: #fff; border-radius: 16px; overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer; border: 1px solid transparent; position: relative;
  
  &:hover {
    transform: translateY(-8px); box-shadow: 0 12px 30px -8px rgba(0, 0, 0, 0.1);
    border-color: rgba(16, 185, 129, 0.2);
  }

  .image-wrapper {
    position: relative; aspect-ratio: 1; background: #f3f4f6; overflow: hidden;
    img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
    &:hover img { transform: scale(1.05); }
    .condition-tag {
      position: absolute; top: 12px; left: 12px;
      background: rgba(255, 255, 255, 0.9); color: #059669; font-size: 12px; font-weight: 600;
      padding: 4px 8px; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
  }

  .info {
    padding: 16px;
    .title {
      font-size: 16px; color: #1f2937; margin: 0 0 12px; line-height: 1.5; height: 48px;
      display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; font-weight: 500;
    }
    .price-row {
      display: flex; align-items: baseline; margin-bottom: 12px;
      .currency { font-size: 14px; color: #ef4444; font-weight: bold; margin-right: 2px;}
      .price { font-size: 20px; color: #ef4444; font-weight: 800; margin-right: auto; }
      .view-count { font-size: 12px; color: #9ca3af; background: #f3f4f6; padding: 2px 6px; border-radius: 4px;}
    }
    .seller-row {
      display: flex; align-items: center; gap: 8px; padding-top: 12px; border-top: 1px solid #f9fafb;
      .name { font-size: 13px; color: #4b5563; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .time { font-size: 12px; color: #9ca3af; }
    }
  }
}

/* 4. 分页区域样式 */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 40px 0 60px;
  
  :deep(.custom-pagination) {
    /* 自定义 Element Plus 分页样式微调 */
    .el-pagination__total { margin-right: 16px; color: #6b7280; }
    .el-pager li {
      background: #fff; border: 1px solid #e5e7eb; font-weight: 500;
      &:hover { color: #10b981; border-color: #10b981; }
      &.is-active { background-color: #10b981 !important; border-color: #10b981; color: #fff; }
    }
    .btn-prev, .btn-next { background: #fff; border: 1px solid #e5e7eb; }
  }
}

.empty-section {
  padding: 60px 0;
}
</style>