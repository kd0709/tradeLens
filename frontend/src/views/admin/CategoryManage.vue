
<template>
  <div class="category-manage">
    <el-card shadow="hover">
      <div class="toolbar">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索分类名称"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #append>
            <el-button icon="Search" @click="handleSearch" />
          </template>
        </el-input>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增分类</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="分类ID" width="100" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="level" label="层级" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">一级分类</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="(val) => handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog
      :title="dialogType === 'add' ? '新增分类' : '编辑分类'"
      v-model="dialogVisible"
      width="400px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="层级" prop="level">
          <el-input-number v-model="formData.level" :min="1" :max="3" disabled />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">当前仅支持一级分类</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getSystemCategoryPage, addSystemCategory, updateSystemCategory, deleteSystemCategory } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  keyword: '',
  page: 1,
  size: 10
})

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  level: 1,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getSystemCategoryPage(queryParams)
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

const handleStatusChange = async (row: any) => {
  try {
    await updateSystemCategory({ id: row.id, status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    level: row.level,
    status: row.status
  })
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除分类 [${row.name}] 吗？`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSystemCategory(row.id)
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && queryParams.page > 1) {
        queryParams.page--
      }
      fetchData()
    } catch (error) {}
  }).catch(() => {})
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (dialogType.value === 'add') {
        await addSystemCategory(formData)
        ElMessage.success('新增成功')
      } else {
        await updateSystemCategory(formData)
        ElMessage.success('修改成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
    } finally {
      submitLoading.value = false
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, { id: undefined, name: '', level: 1, status: 1 })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.category-manage {
  padding: 10px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.search-input {
  width: 300px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>