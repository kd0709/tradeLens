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

    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <div ref="salesChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { getSystemDashboardStats } from '@/api/system'
import { ElMessage } from 'element-plus'
import { Money, User, Goods, List } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const statsData = ref<any>({})

// 图表 DOM 引用
const salesChartRef = ref<HTMLElement | null>(null)
const categoryChartRef = ref<HTMLElement | null>(null)

// 图表实例
let salesChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null

const fetchStats = async () => {
  loading.value = true
  try {
    const res = await getSystemDashboardStats()
    statsData.value = res || {}
    
    // 获取数据后初始化图表
    nextTick(() => {
        initCharts(res.trendData, res.categoryData)
    })
  } catch (error) {
    ElMessage.error('获取大盘数据失败')
  } finally {
    loading.value = false
  }
}

// 初始化图表，接收后端真实数据
const initCharts = (trendData: any, categoryData: any) => {
  // --- 折线图 ---
  if (salesChartRef.value) {
    salesChart = echarts.init(salesChartRef.value)
    
    // 如果没有数据，给默认空数组
    const dates = trendData?.dates || []
    const sales = trendData?.sales || []

    const salesOption = {
      title: { text: '近7天交易额趋势', left: '10' },
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates 
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: '交易额',
          type: 'line',
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64,158,255,0.5)' },
              { offset: 1, color: 'rgba(64,158,255,0.1)' }
            ])
          },
          itemStyle: { color: '#409EFF' },
          data: sales 
        }
      ]
    }
    salesChart.setOption(salesOption)
  }

  // --- 饼图 ---
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
    
    const cData = categoryData || [{value: 0, name: '暂无数据'}]

    const categoryOption = {
      title: { text: '商品分类占比', left: 'center' },
      tooltip: { trigger: 'item' },
      legend: { bottom: '0', left: 'center' },
      series: [
        {
          name: '商品数量',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: '20', fontWeight: 'bold' }
          },
          labelLine: { show: false },
          data: cData 
        }
      ]
    }
    categoryChart.setOption(categoryOption)
  }
}

// 窗口自适应
const handleResize = () => {
  salesChart?.resize()
  categoryChart?.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  categoryChart?.dispose()
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

/* 图表区域样式 */
.chart-row {
  margin-top: 20px;
}
.chart-card {
  border-radius: 8px;
}
.chart-container {
  height: 350px;
  width: 100%;
}
</style>