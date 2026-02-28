<template>
  <div class="product-manage">
    <el-card shadow="hover">
      <div class="toolbar">
        <div class="search-group">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索商品标题/描述"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-select 
            v-model="queryParams.productStatus" 
            placeholder="商品状态" 
            clearable 
            class="filter-select"
            @change="handleSearch"
          >
            <el-option label="待审核" :value="1" />
            <el-option label="已上架" :value="2" />
            <el-option label="已下架/违规" :value="3" />
            <el-option label="已售出" :value="4" />
          </el-select>
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="商品ID" width="80" align="center" />
        <el-table-column prop="title" label="商品标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="price" label="价格(元)" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="conditionLevel" label="成色" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" effect="plain">{{ getConditionText(row.conditionLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商品状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.productStatus)">
              {{ getStatusText(row.productStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" align="center" />
        
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.productStatus === 1">
              <el-button type="success" link icon="Check" @click="handleAudit(row, 2)">通过</el-button>
              <el-button type="danger" link icon="Close" @click="handleAudit(row, 3)">驳回</el-button>
            </template>
            <template v-else-if="row.productStatus === 2">
              <el-button type="warning" link icon="WarnTriangleFilled" @click="handleForceOffline(row)">违规下架</el-button>
            </template>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getSystemProductPage, updateSystemProduct, deleteSystemProduct } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, WarnTriangleFilled, Delete, Search } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  keyword: '',
  productStatus: undefined as number | undefined,
  page: 1,
  size: 10
})

// 字典转换：商品成色
const getConditionText = (level: number) => {
  const map: Record<number, string> = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显使用' }
  return map[level] || '未知'
}

// 字典转换：商品状态
const getStatusText = (status: number) => {
  const map: Record<number, string> = { 1: '待审核', 2: '已上架', 3: '已下架', 4: '已售出' }
  return map[status] || '未知'
}
const getStatusType = (status: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

// 获取商品列表
const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getSystemProductPage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// 审核操作 (2:通过上架, 3:驳回下架)
const handleAudit = async (row: any, targetStatus: number) => {
  const actionText = targetStatus === 2 ? '通过审核并上架' : '驳回审核并下架'
  try {
    await ElMessageBox.confirm(`确定要对商品 [${row.title}] 执行【${actionText}】操作吗？`, '审核提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: targetStatus === 2 ? 'success' : 'warning'
    })
    
    await updateSystemProduct({ id: row.id, productStatus: targetStatus })
    ElMessage.success(`操作成功`)
    fetchData()
  } catch (error) {
    // 用户取消或请求失败
  }
}

// 强制下架违规商品
const handleForceOffline = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要强制下架违规商品 [${row.title}] 吗？这会导致买家无法继续购买。`, '违规下架警告', {
      confirmButtonText: '强制下架',
      cancelButtonText: '取消',
      type: 'danger'
    })
    
    await updateSystemProduct({ id: row.id, productStatus: 3 })
    ElMessage.success(`已强制下架`)
    fetchData()
  } catch (error) {}
}

// 逻辑删除商品
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除商品 [${row.title}] 吗？删除后该商品数据将不可恢复。`, '危险操作', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await deleteSystemProduct(row.id)
    ElMessage.success('删除成功')
    if (tableData.value.length === 1 && queryParams.page > 1) {
      queryParams.page--
    }
    fetchData()
  } catch (error) {}
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.product-manage {
  padding: 10px;
}
.toolbar {
  margin-bottom: 20px;
}
.search-group {
  display: flex;
  gap: 10px;
}
.search-input {
  width: 300px;
}
.filter-select {
  width: 150px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>