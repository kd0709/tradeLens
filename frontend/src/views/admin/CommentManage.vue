<template>
  <div class="comment-manage">
    <el-card shadow="hover">
      <div class="toolbar">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索评价内容"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #append>
            <el-button icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="content" label="评价内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="score" label="评分" width="150" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.score" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column label="关联信息" min-width="150">
          <template #default="{ row }">
            <div>商品ID: {{ row.productId }}</div>
            <div>用户ID: {{ row.userId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="170" align="center" />
        
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除违规</el-button>
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
import { getSystemCommentPage, deleteSystemComment } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  keyword: '',
  page: 1,
  size: 10
})

// 获取评价列表
const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getSystemCommentPage(queryParams)
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

// 删除违规评价
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除该条评价吗？删除后不可恢复。`, '清理警告', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteSystemComment(row.id)
    ElMessage.success('已删除违规评价')
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
.comment-manage {
  padding: 10px;
}
.toolbar {
  margin-bottom: 20px;
}
.search-input {
  width: 350px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>