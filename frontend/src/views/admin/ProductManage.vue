<template>
  <div class="product-manage-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="商品标题">
          <el-input v-model="queryParams.keyword" placeholder="请输入商品标题" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="商品状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="待审核" :value="1" />
            <el-option label="已上架" :value="2" />
            <el-option label="已下架" :value="3" />
            <el-option label="已售出" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="success" @click="handleExport" :loading="exportLoading">
            <el-icon><Download /></el-icon> 导出 Excel
          </el-button>          
          <el-button type="warning" @click="router.push('/admin/audit')">
            <el-icon><Stamp /></el-icon> 去审核商品
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="商品标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="price" label="价格(元)" width="100" align="center">
          <template #default="{ row }">
            <span class="text-danger font-bold">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="库存" width="80" align="center" />
        <el-table-column label="新旧程度" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" effect="plain">{{ getConditionText(row.conditionLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.productStatus)" effect="dark">
              {{ getStatusText(row.productStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button 
              v-if="row.productStatus === 2" 
              type="warning" 
              link 
              @click="handleForceOffline(row)"
            >
              <el-icon><Bottom /></el-icon> 违规下架
            </el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSearch"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download, Delete, Bottom ,Stamp} from '@element-plus/icons-vue'
import request from '@/api/request'
import { exportExcel } from '@/utils/export' // 引入我们之前封装的通用导出工具
import { useRouter } from 'vue-router'


const router = useRouter()



// 响应式状态
const loading = ref(false)
const exportLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const queryParams = ref({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined
})

// 字典翻译
const getConditionText = (level: number) => {
  const map: Record<number, string> = { 1: '全新', 2: '99新', 3: '9成新', 4: '8成新' }
  return map[level] || '二手'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 1: '待审核', 2: '已上架', 3: '已下架', 4: '已售出' }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

// 获取表格数据
const loadData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/system/product/page', {
      params: queryParams.value
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取商品列表失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索与重置
const handleSearch = () => {
  queryParams.value.page = 1
  loadData()
}

const handleReset = () => {
  queryParams.value = { page: 1, size: 10, keyword: '', status: undefined }
  loadData()
}

// 导出 Excel 逻辑
const handleExport = async () => {
  exportLoading.value = true
  // 将当前的搜索条件传给后端，实现“按条件导出”
  await exportExcel('/api/system/product/export', '商品信息列表.xlsx', queryParams.value)
  exportLoading.value = false
}

// 强制下架违规商品
const handleForceOffline = (row: any) => {
  ElMessageBox.confirm(
    `确定要强制下架商品 "${row.title}" 吗？此操作将通知卖家。`,
    '违规下架警告',
    { confirmButtonText: '确定下架', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      // 【核心修改】：改为 productId 和 pass: false
      await request.put('/api/system/product/audit', { 
        productId: row.id, 
        pass: false 
      })
      ElMessage.success('商品已强制下架')
      loadData()
    } catch (error) {
      console.error('下架失败', error)
    }
  }).catch(() => {})
}

// 删除商品
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要永久删除商品 "${row.title}" 吗？此操作不可逆！`,
    '危险操作',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }
  ).then(async () => {
    try {
      await request.delete(`/api/system/product/${row.id}`)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.product-manage-container {
  padding: 20px;
}
.search-card {
  margin-bottom: 20px;
}
.table-card {
  min-height: 500px;
}
.text-danger {
  color: #f56c6c;
}
.font-bold {
  font-weight: bold;
}
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>