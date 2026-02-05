<template>
  <div class="publish-page">
    <div class="container">
      <div class="page-header">
        <h2>发布闲置</h2>
        <p>让你的闲置物品找到新主人</p>
      </div>

      <div class="form-card">
        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          label-position="top"
          size="large"
        >
          <el-form-item label="商品图片 (首张为主图)" prop="images">
            <el-upload
              v-model:file-list="fileList"
              action="#" 
              list-type="picture-card"
              :auto-upload="true"
              :http-request="handleUpload"
              :on-remove="handleRemove"
              :limit="9"
              class="custom-upload"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="tip-text">支持 jpg/png，单张不超过 5MB（已上传 {{ uploadedImages.length }} 张）</div>
          </el-form-item>

          <el-form-item label="标题" prop="title">
            <el-input 
              v-model="form.title" 
              placeholder="品牌型号，如：iPhone 13 128G 白色" 
              maxlength="50" 
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="详细描述" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="4" 
              placeholder="描述一下宝贝的来源、使用情况、是否有瑕疵..." 
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="价格" prop="price">
                <el-input-number 
                  v-model="form.price" 
                  :min="0" 
                  :precision="2" 
                  :step="10" 
                  style="width: 100%"
                  placeholder="0.00"
                >
                  <template #prefix>¥</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
               <el-form-item label="成色" prop="conditionLevel">
                <el-select v-model="form.conditionLevel" placeholder="请选择" style="width: 100%">
                  <el-option label="全新" :value="1" />
                  <el-option label="几乎全新" :value="2" />
                  <el-option label="轻微使用" :value="3" />
                  <el-option label="明显使用" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="分类" prop="categoryId">
             <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%" @focus="loadCategories">
              <el-option 
                v-for="cat in categories" 
                :key="cat.id" 
                :label="cat.name" 
                :value="cat.id" 
              />
            </el-select>
          </el-form-item>

          <div class="form-actions">
            <el-button @click="router.back()">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitForm">
              立即发布
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadRequestOptions, UploadUserFile } from 'element-plus'
import { uploadFile } from '@/api/common'
import { publishProduct } from '@/api/product'
import { getCategoryList } from '@/api/category'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const fileList = ref<UploadUserFile[]>([])
const categories = ref<Array<{ id: number; name: string }>>([])

// 🔥 关键修改：使用独立数组存储已上传成功的图片URL
const uploadedImages = ref<string[]>([])

const form = reactive({
  title: '',
  description: '',
  price: 0,
  categoryId: undefined as number | undefined,
  conditionLevel: 2,
  images: [] as string[]
})

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  conditionLevel: [{ required: true, message: '请选择成色', trigger: 'change' }],
  images: [{ 
    required: true, 
    validator: (_rule: any, _value: any, callback: any) => {
      if (uploadedImages.value.length === 0) {
        callback(new Error('请至少上传一张图片'))
      } else {
        callback()
      }
    },
    trigger: 'change' 
  }]
}

// 删除文件时，同时从 uploadedImages 中移除对应的 URL
const handleRemove = (uploadFile: UploadUserFile) => {
  
  // 从 uploadedImages 中移除对应的 URL
  const resp = uploadFile.response
  let urlToRemove = null
  
  if (resp && typeof resp === 'object' && 'url' in resp) {
    urlToRemove = resp.url
  } else if (typeof resp === 'string') {
    urlToRemove = resp
  }
  
  if (urlToRemove) {
    const index = uploadedImages.value.indexOf(urlToRemove)
    if (index > -1) {
      uploadedImages.value.splice(index, 1)
    }
  }
}

// 自定义上传方法
const handleUpload = async (options: UploadRequestOptions) => {
  try {
    const result = await uploadFile(options.file)
    
    // 处理不同的返回格式
    let url: string = ''
    if (typeof result === 'string') {
      url = result
    } else if (result && typeof result === 'object') {
      // 可能是 { url: "..." } 或 { data: "..." } 等格式
      url = (result as any).url || (result as any).data || String(result)
    }
    
    if (!url) {
      throw new Error('上传返回的URL格式不正确')
    }
    
    // 🔥 关键：将上传成功的 URL 添加到 uploadedImages
    uploadedImages.value.push(url)
    
    // 调用 onSuccess，传递 URL
    options.onSuccess({ url })
    
    // 触发表单验证更新
    formRef.value?.validateField('images')
  } catch (error) {
    console.error('❌ 上传失败:', error)
    options.onError(error as any)
    ElMessage.error('图片上传失败，请重试')
  }
}

const loadCategories = async () => {
  if (categories.value.length > 0) return
  try {
    const catList = await getCategoryList()
    categories.value = catList.map((cat: any) => ({ id: cat.id, name: cat.name }))
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

onMounted(() => {
  loadCategories()
})

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return


    // 直接使用 uploadedImages 数组，不再依赖 fileList 的 status
    if (uploadedImages.value.length === 0) {
      ElMessage.warning('请至少上传一张图片')
      return
    }

    submitting.value = true
    try {
      await publishProduct({
        title: form.title,
        description: form.description,
        price: form.price,
        categoryId: form.categoryId!,
        conditionLevel: form.conditionLevel,
        images: uploadedImages.value  // 🔥 直接使用 uploadedImages
      })

      ElMessage.success('发布成功！')
      router.push('/')
    } catch (error: any) {
      console.error('❌ 发布失败:', error)
      ElMessage.error(error.response?.data?.message || '发布失败，请检查网络')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.publish-page {
  background-color: #f9fafb;
  min-height: calc(100vh - 64px);
  padding: 40px 20px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 { font-size: 28px; color: #111827; margin: 0 0 8px; }
  p { color: #6b7280; font-size: 16px; margin: 0; }
}

.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.tip-text {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #f3f4f6;
  
  button {
    min-width: 100px;
  }
}

/* 覆盖 Element Upload 样式以匹配极简风 */
:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  border: 1px dashed #d1d5db;
  
  &:hover { border-color: #10b981; color: #10b981; }
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
  border-radius: 8px;
}
</style>