<template>
  <div class="product-audit-container">
    <el-card>
      <template #header>
        <div class="header-wrapper">
          <span>待审核商品流水线</span>
          <el-tag type="warning">当前待审: {{ tableData.length }} 条</el-tag>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="商品ID" width="80" />
        <el-table-column prop="title" label="商品标题" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row, $index }">
            <el-button type="primary" size="small" @click="openAuditDialog(row, $index)">
              开始审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      title="商品详细信息审核"
      width="700px"
      :close-on-click-modal="false"
      destroy-on-close
      top="5vh"
    >
      <div v-if="currentItem" class="audit-detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="商品ID" :width="100">{{ currentItem.id }}</el-descriptions-item>
          <el-descriptions-item label="卖家信息" :width="100">
            {{ currentItem.sellerNickname || currentItem.userId || '未知卖家' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="商品标题" :span="2">
            <span class="font-bold">{{ currentItem.title }}</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="售价">
            <span class="text-danger font-bold">¥{{ currentItem.price }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="库存数量">
            {{ currentItem.quantity }} 件
          </el-descriptions-item>
          
          <el-descriptions-item label="新旧程度">
            <el-tag size="small" type="info">{{ getConditionText(currentItem.conditionLevel) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">
            {{ formatTime(currentItem.createTime) }}
          </el-descriptions-item>
          
          <el-descriptions-item label="商品描述" :span="2">
            <div class="desc-text">{{ currentItem.description || '卖家很懒，没有填写详细描述...' }}</div>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="image-section">
          <div class="section-title">商品图片 (点击可放大预览)</div>
          
          <div class="images" v-if="currentItem.mainImage">
            <el-image 
              class="audit-img"
              :src="getFullImageUrl(currentItem.mainImage)"
              :preview-src-list="[getFullImageUrl(currentItem.mainImage)]"
              fit="cover"
            />
          </div>
          
          <div class="images" v-else-if="currentItem.images && currentItem.images.length > 0">
            <el-image 
              v-for="(img, idx) in currentItem.images" 
              :key="idx"
              class="audit-img"
              :src="getFullImageUrl(img)"
              :preview-src-list="currentItem.images.map(i => getFullImageUrl(i))"
              fit="cover"
              :initial-index="idx"
            />
          </div>
          
          <el-empty v-else description="该商品未上传图片" :image-size="80" />
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <div class="left-actions">
            <el-button type="danger" plain @click="submitAudit(3, false)" :loading="submitLoading">仅驳回</el-button>
            <el-button type="success" plain @click="submitAudit(2, false)" :loading="submitLoading">仅通过</el-button>
          </div>
          <div class="right-actions">
            <el-button type="danger" @click="submitAudit(3, true)" :loading="submitLoading">驳回，并审核下一条</el-button>
            <el-button type="success" @click="submitAudit(2, true)" :loading="submitLoading">通过，并审核下一条</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { getFullImageUrl } from '@/utils/image'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const currentIndex = ref<number>(-1)

const currentItem = computed(() => {
  if (currentIndex.value >= 0 && currentIndex.value < tableData.value.length) {
    return tableData.value[currentIndex.value]
  }
  return null
})

const getConditionText = (level: number) => {
  const map: Record<number, string> = { 1: '全新', 2: '99新', 3: '9成新', 4: '8成新' }
  return map[level] || '二手'
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

const loadPendingProducts = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/system/product/page', {
      params: { page: 1, size: 50, productStatus: 1 } 
    })
    tableData.value = res.records || []
  } catch (error) {
    console.error('获取待审核列表失败', error)
  } finally {
    loading.value = false
  }
}

const openAuditDialog = (row: any, index: number) => {
  currentIndex.value = index
  dialogVisible.value = true
}

const submitAudit = async (targetStatus: number, autoNext: boolean) => {
  if (!currentItem.value) return

  submitLoading.value = true
  try {
    await request.put('/api/system/product/audit', {
      productId: currentItem.value.id,
      pass: targetStatus === 2
    })

    ElMessage.success(targetStatus === 2 ? '已通过' : '已驳回')
    tableData.value.splice(currentIndex.value, 1)

    if (autoNext) {
      if (tableData.value.length > 0) {
        if (currentIndex.value >= tableData.value.length) {
          currentIndex.value = 0
        }
      } else {
        ElMessage.success('太棒了，所有商品均已审核完毕！')
        dialogVisible.value = false
      }
    } else {
      dialogVisible.value = false
    }
  } catch (error) {
    console.error('审核操作失败', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadPendingProducts()
})
</script>

<style scoped>
.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.audit-detail {
  padding: 0 10px;
}
.font-bold {
  font-weight: bold;
}
.text-danger {
  color: #f56c6c;
  font-size: 16px;
}
.desc-text {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.5;
  color: #606266;
  max-height: 100px;
  overflow-y: auto;
}
.image-section {
  margin-top: 20px;
}
.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  border-left: 3px solid #409EFF;
  padding-left: 8px;
}
.images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.audit-img {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  cursor: zoom-in;
}
.dialog-footer {
  display: flex;
  justify-content: space-between;
  width: 100%;
}
</style>