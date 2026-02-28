<template>
  <div class="user-manage">
    <el-card shadow="hover">
      <div class="toolbar">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索用户名/昵称/手机号"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #append>
            <el-button icon="Search" @click="handleSearch" />
          </template>
        </el-input>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'info'">
              {{ row.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="row.role === 1"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="(val) => handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              type="danger" 
              link 
              icon="Delete" 
              :disabled="row.role === 1"
              @click="handleDelete(row)"
            >删除</el-button>
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
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      v-model="dialogVisible"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="dialogType === 'edit'" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" placeholder="留空则不修改 / 新增默认123456" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入用户昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="formData.role">
            <el-radio :label="0">普通用户</el-radio>
            <el-radio :label="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">封禁</el-radio>
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
import { getSystemUserPage, updateSystemUser, deleteSystemUser, addSystemUser } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

// --- 列表数据 ---
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  keyword: '',
  page: 1,
  size: 10
})

// --- 表单数据 ---
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive({
  id: undefined as number | undefined,
  username: '',
  password: '',
  nickname: '',
  phone: '',
  role: 0,
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getSystemUserPage(queryParams)
    tableData.value = res.records // 注意 MyBatis-Plus 的 Page 对象数据在 records 里
    total.value = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

// 状态快捷切换
const handleStatusChange = async (row: any) => {
  try {
    await updateSystemUser({ id: row.id, status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error) {
    // 失败则回滚开关状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 打开新增弹窗
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 打开编辑弹窗
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    password: '', // 密码不回显
    nickname: row.nickname,
    phone: row.phone,
    role: row.role,
    status: row.status
  })
  dialogVisible.value = true
}

// 删除用户
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除用户 [${row.username}] 吗？此操作不可逆！`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSystemUser(row.id)
      ElMessage.success('删除成功')
      // 如果当前页只有一条数据且不是第一页，则往前翻一页
      if (tableData.value.length === 1 && queryParams.page > 1) {
        queryParams.page--
      }
      fetchData()
    } catch (error) {}
  }).catch(() => {})
}

// 提交表单
const handleSubmit = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (dialogType.value === 'add') {
        await addSystemUser(formData)
        ElMessage.success('新增成功')
      } else {
        await updateSystemUser(formData)
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

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, { id: undefined, username: '', password: '', nickname: '', phone: '', role: 0, status: 1 })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-manage {
  padding: 10px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
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