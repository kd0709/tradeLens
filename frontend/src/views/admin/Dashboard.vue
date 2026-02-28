<template>
  <div class="dashboard-container">
    <h2 class="page-title">数据概览</h2>
    
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>累计交易额 (元)</span>
            <el-icon color="#409EFF"><Money /></el-icon>
          </div>
          <div class="card-value text-primary">
            ¥ {{ statsData.totalSales?.toFixed(2) || '0.00' }}
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>平台总用户数</span>
            <el-icon color="#67C23A"><User /></el-icon>
          </div>
          <div class="card-value text-success">
            {{ statsData.totalUsers || 0 }}
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>当前在售商品</span>
            <el-icon color="#E6A23C"><Goods /></el-icon>
          </div>
          <div class="card-value text-warning">
            {{ statsData.activeProducts || 0 }}
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>历史总订单数</span>
            <el-icon color="#F56C6C"><List /></el-icon>
          </div>
          <div class="card-value text-danger">
            {{ statsData.totalOrders || 0 }}
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSystemDashboardStats } from '@/api/system'
import { ElMessage } from 'element-plus'
import { Money, User, Goods, List } from '@element-plus/icons-vue'

const loading = ref(false)
const statsData = ref<any>({})

const fetchStats = async () => {
  loading.value = true
  try {
    const res = await getSystemDashboardStats()
    statsData.value = res || {}
  } catch (error) {
    ElMessage.error('获取大盘数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}
.page-title {
  margin-bottom: 20px;
  color: #303133;
}
.data-card {
  border-radius: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}
.card-value {
  font-size: 28px;
  font-weight: bold;
}
.text-primary { color: #409EFF; }
.text-success { color: #67C23A; }
.text-warning { color: #E6A23C; }
.text-danger { color: #F56C6C; }
</style>