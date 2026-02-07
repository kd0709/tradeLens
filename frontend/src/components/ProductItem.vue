<template>
  <div class="product-item" @click="handleDetail">
    <el-card :body-style="{ padding: '0px' }" shadow="hover" class="box-card">
      <div class="image-wrapper">
        <el-image 
          :src="getFullImageUrl(product.mainImage)" 
          fit="cover" 
          loading="lazy"
          class="product-img"
        >
          <template #error>
            <div class="image-slot">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-image>
      </div>
      <div class="product-info">
        <h3 class="product-title">{{ product.title }}</h3>
        <div class="product-bottom">
          <span class="price">¥ {{ product.price }}</span>
          <div class="seller-info" v-if="product.seller">
            <el-avatar :size="20" :src="getFullImageUrl(product.seller.avatar)" />
            <span class="seller-name">{{ product.seller.nickname }}</span>
          </div>
        </div>
        <div class="product-meta">
          <span class="view-count">{{ product.viewCount || 0 }}人想要</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { getFullImageUrl } from '@/utils/image'
import { Picture } from '@element-plus/icons-vue'

const props = defineProps<{
  product: any 
}>()

const router = useRouter()

const handleDetail = () => {
  router.push(`/product/${props.product.id}`)
}
</script>

<style scoped>
.product-item {
  cursor: pointer;
  transition: all 0.3s;
}

.product-item:hover {
  transform: translateY(-5px);
}

.box-card {
  border-radius: 12px;
  overflow: hidden;
  border: none;
}

.image-wrapper {
  width: 100%;
  height: 0;
  padding-bottom: 100%;
  position: relative;
  background-color: #f5f7fa;
}

.product-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #909399;
  font-size: 24px;
}

.product-info {
  padding: 12px;
}

.product-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  height: 42px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: bold;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.seller-name {
  font-size: 12px;
  color: #909399;
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  font-size: 12px;
  color: #c0c4cc;
}
</style>