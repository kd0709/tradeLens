
<template>
  <div class="order-manage">

    <div class="header-actions">
      <el-button type="success" @click="handleExport" :loading="exportLoading">
        <el-icon><Download /></el-icon>导出 Excel
      </el-button>
    </div>

    <el-card shadow="hover">
      <div class="toolbar">
        <div class="search-group">
          <el-input
            v-model="queryParams.orderNo"
            placeholder="搜索订单号"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-select 
            v-model="queryParams.status" 
            placeholder="订单状态" 
            clearable 
            class="filter-select"
            @change="handleSearch"
          >
            <el-option label="待支付" :value="1" />
            <el-option label="待发货" :value="2" />
            <el-option label="已发货" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="orderNo" label="订单编号" min-width="180" />
        <el-table-column label="买卖双方" min-width="150">
          <template #default="{ row }">
            <div>买家ID: {{ row.buyerId }}</div>
            <div>卖家ID: {{ row.sellerId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="订单总价" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        
        <el-table-column label="操作(管理员介入)" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status !== 4 && row.status !== 5"
              type="danger" 
              link 
              icon="CircleClose" 
              @click="handleForceCancel(row)"
            >强制取消</el-button>
            <span v-else style="color: #909399; font-size: 13px;">无可用操作</span>
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
import { getSystemOrdersPage, updateSystemOrder } from '@/api/system'
import { Download } from '@element-plus/icons-vue'
import { exportExcel } from '@/utils/export'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const exportLoading = ref(false)


const queryParams = reactive({
  orderNo: '',
  status: undefined as number | undefined,
  page: 1,
  size: 10
})

// 字典转换：订单状态
const getStatusText = (status: number) => {
  const map: Record<number, string> = { 1: '待支付', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消' }
  return map[status] || '未知'
}
const getStatusType = (status: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger' }
  return map[status] || 'info'
}

// 获取订单列表
const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getSystemOrdersPage(queryParams)
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

// 管理员强制取消订单
const handleForceCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要强制取消订单 [${row.orderNo}] 吗？请确保已处理好退款或纠纷。`, '强制取消警告', {
      confirmButtonText: '确定取消',
      cancelButtonText: '暂不处理',
      type: 'error'
    })
    
    await updateSystemOrder({ id: row.id, status: 5 }) // 5代表已取消
    ElMessage.success(`订单已强制取消`)
    fetchData()
  } catch (error) {}
}


const handleExport = async () => {
  exportLoading.value = true
  await exportExcel('/api/system/orders/export', '订单信息导出.xlsx')
  exportLoading.value = false
}


onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.order-manage {
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
.header-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}
</style>