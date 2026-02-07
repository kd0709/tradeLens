<template>
  <div class="publish-page">
    <div class="bg-decoration left"></div>
    <div class="bg-decoration right"></div>

    <div class="container">
      <Transition name="fade-down" appear>
        <div class="page-header">
          <h2>发布闲置</h2>
          <p>让你的闲置物品找到新主人</p>
        </div>
      </Transition>

      <Transition name="fade-up" appear>
        <div class="form-card">
          <el-form 
            ref="formRef" 
            :model="form" 
            :rules="rules" 
            label-position="top"
            size="large"
            class="publish-form"
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
                :rows="5" 
                placeholder="描述一下宝贝的来源、使用情况、是否有瑕疵..." 
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="价格" prop="price">
                  <el-input-number 
                    v-model="form.price" 
                    :min="0" 
                    :precision="2" 
                    :step="10" 
                    style="width: 100%"
                    placeholder="0.00"
                    :controls="false"
                  >
                    <template #prefix>¥</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="数量" prop="quantity">
                  <el-input-number 
                    v-model="form.quantity" 
                    :min="1" 
                    :max="999"
                    :step="1" 
                    style="width: 100%"
                    placeholder="库存数量"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="8">
                 <el-form-item label="成色" prop="conditionLevel">
                  <el-select v-model="form.conditionLevel" placeholder="请选择" style="width: 100%">
                    <el-option label="全新" :value="1" />
                    <el-option label="几乎全新" :value="2" />
                    <el-option label="轻微使用" :value="3" />
                    <el-option label="明显使用" :value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="交易方式" prop="negotiable">
                  <el-select v-model="form.negotiable" placeholder="是否可议价" style="width: 100%">
                    <el-option label="可小刀 (接受议价)" :value="1" />
                    <el-option label="一口价 (不讲价)" :value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="分类" prop="categoryId">
                   <el-select 
                      v-model="form.categoryId" 
                      placeholder="选择分类" 
                      style="width: 100%" 
                      @focus="loadCategories"
                    >
                    <el-option 
                      v-for="cat in categories" 
                      :key="cat.id" 
                      :label="cat.name" 
                      :value="cat.id" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <div class="form-actions">
              <el-button size="large" @click="router.back()">取 消</el-button>
              <el-button 
                type="primary" 
                size="large" 
                :loading="submitting" 
                @click="submitForm"
                class="submit-btn"
              >
                立即发布
              </el-button>
            </div>
          </el-form>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 模块化：导入
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadRequestOptions, UploadUserFile } from 'element-plus'
import { uploadFile } from '@/api/common'
import { publishProduct } from '@/api/product'
import { getCategoryList } from '@/api/category'
// 🔥 导入类型定义
import type { ProductPublishDto } from '@/dto/product'

/**
 * 模块化：状态管理
 */
const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const fileList = ref<UploadUserFile[]>([])
const categories = ref<Array<{ id: number; name: string }>>([])
const uploadedImages = ref<string[]>([]) 

// 🔥 使用 ProductPublishDto 类型定义表单数据
// Partial 是因为初始状态下某些必填字段可能为空（如 categoryId），直到用户填写
const form = reactive<Partial<ProductPublishDto>>({
  title: '',
  description: '',
  price: undefined,     
  quantity: 1,          // 默认库存1
  negotiable: 1,        // 默认可议价
  categoryId: undefined,
  conditionLevel: 2,    // 默认几乎全新
  images: []
})

/**
 * 模块化：验证规则
 */
const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  conditionLevel: [{ required: true, message: '请选择成色', trigger: 'change' }],
  negotiable: [{ required: true, message: '请选择交易方式', trigger: 'change' }],
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

/**
 * 模块化：上传逻辑
 */
const handleRemove = (uploadFile: UploadUserFile) => {
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

const handleUpload = async (options: UploadRequestOptions) => {
  try {
    const result = await uploadFile(options.file)
    
    let url: string = ''
    if (typeof result === 'string') {
      url = result
    } else if (result && typeof result === 'object') {
      url = (result as any).url || (result as any).data || String(result)
    }
    
    if (!url) throw new Error('上传返回格式错误')
    
    uploadedImages.value.push(url)
    options.onSuccess({ url })
    formRef.value?.validateField('images') 
  } catch (error) {
    console.error('❌ 上传失败:', error)
    options.onError(error as any)
    ElMessage.error('图片上传失败，请重试')
  }
}

/**
 * 模块化：数据加载与提交
 */
const loadCategories = async () => {
  if (categories.value.length > 0) return
  try {
    const catList = await getCategoryList()
    categories.value = catList.map((cat: any) => ({ id: cat.id, name: cat.name }))
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    if (uploadedImages.value.length === 0) {
      ElMessage.warning('请至少上传一张图片')
      return
    }

    submitting.value = true
    try {
      // 构造提交数据，显式指定类型确保字段匹配
      // 使用 ! 断言必填字段不为空（已通过 validate 校验）
      const payload: ProductPublishDto = {
        title: form.title!,
        description: form.description || '',
        price: form.price!,
        quantity: form.quantity || 1,
        negotiable: (form.negotiable as 0 | 1) ?? 1,
        categoryId: form.categoryId!,
        conditionLevel: form.conditionLevel!,
        images: uploadedImages.value
      }

      await publishProduct(payload)

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

onMounted(() => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.publish-page {
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  min-height: calc(100vh - 64px);
  padding: 40px 20px;
  position: relative;
  overflow: hidden;

  /* 背景装饰 */
  .bg-decoration {
    position: absolute;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.5;
    z-index: 0;
    
    &.left {
      background: #d1fae5;
      top: -100px;
      left: -100px;
    }
    &.right {
      background: #e0e7ff;
      bottom: -100px;
      right: -100px;
    }
  }
}

.container {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 { 
    font-size: 32px; 
    color: #111827; 
    margin: 0 0 12px; 
    font-weight: 800;
  }
  p { color: #6b7280; font-size: 16px; margin: 0; }
}

.form-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
  border: 1px solid #fff;
}

.tip-text {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 48px;
  padding-top: 24px;
  border-top: 1px solid rgba(0,0,0,0.05);
  
  .submit-btn {
    min-width: 140px;
    font-weight: 600;
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    border: none;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
    }
  }
}

/* 覆盖 Element 样式 */
:deep(.el-upload--picture-card) {
  width: 110px;
  height: 110px;
  border-radius: 12px;
  border: 2px dashed #e5e7eb;
  background-color: #f9fafb;
  transition: all 0.3s;
  
  &:hover { 
    border-color: #10b981; 
    color: #10b981; 
    background-color: #ecfdf5;
  }
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 110px;
  height: 110px;
  border-radius: 12px;
}

:deep(.el-input__wrapper), :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #e5e7eb;
  &:hover { box-shadow: 0 0 0 1px #d1d5db; }
  &.is-focus { box-shadow: 0 0 0 1px #10b981 !important; }
}

/* 动画定义 */
.fade-down-enter-active, .fade-up-enter-active {
  transition: all 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.fade-down-enter-from {
  opacity: 0;
  transform: translateY(-30px);
}
.fade-up-enter-from {
  opacity: 0;
  transform: translateY(40px);
}
</style>