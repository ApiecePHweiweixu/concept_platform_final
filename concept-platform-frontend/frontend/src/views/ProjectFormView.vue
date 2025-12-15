<template>
  <div class="project-form-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目申报</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" />
        </el-form-item>

        <el-form-item label="技术领域" prop="techDomain">
          <el-input v-model="form.techDomain" placeholder="请输入技术领域" />
        </el-form-item>

        <el-form-item label="项目简介" prop="description">
          <el-input v-model="form.description" type="textarea" rows="4" placeholder="请输入项目简介" />
        </el-form-item>

        <el-form-item label="附件上传" prop="attachmentUrl">
          <!-- 
            action: 上传接口地址，硬编码为后端地址
            on-success: 上传成功后的回调
            limit: 限制上传1个文件
          -->
          <el-upload
            class="upload-demo"
            action="http://localhost:8080/api/common/upload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :limit="1"
            :file-list="fileList"
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 jpg/png/pdf/doc 文件，且不超过 500kb
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">立即申报</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { addProject } from '@/api/project'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const fileList = ref([])

const form = reactive({
  projectName: '',
  techDomain: '',
  description: '',
  attachmentUrl: '' // 存储上传成功后的 URL
})

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  techDomain: [{ required: true, message: '请输入技术领域', trigger: 'blur' }],
  attachmentUrl: [{ required: true, message: '请上传附件', trigger: 'change' }]
}

// 上传成功回调
const handleUploadSuccess = (response, uploadFile) => {
  // 假设后端返回格式为 { code: 200, msg: "success", data: "http://xxx/file.pdf" }
  if (response.code === 200) {
    form.attachmentUrl = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
    // 如果失败，可以清空文件列表
    fileList.value = []
  }
}

const handleUploadError = (error) => {
  console.error(error)
  ElMessage.error('上传失败，请检查网络或后端接口')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 1. 获取用户信息
        const userStr = localStorage.getItem('user')
        let user = {}
        if (userStr) {
          try {
            user = JSON.parse(userStr)
          } catch (e) {
            console.error('JSON Parse Error', e)
          }
        }
        
        // 关键点：打印调试
        console.log('Current User:', user)

        // 2. 构建提交数据
        const submitData = {
          projectName: form.projectName,
          description: form.description,
          techDomain: form.techDomain,
          attachmentUrl: form.attachmentUrl,
          status: 1 // 强制设置为 1 (待初审)
        }

        // 3. 强制赋值 applicantId
        // 尝试多种可能的字段名
        const userId = user.userId || user.user_id || user.id

        if (userId) {
          submitData.applicantId = userId
        } else {
          // 保底策略
          submitData.applicantId = 1
          ElMessage.warning('当前未读取到用户ID，使用测试ID: 1')
        }

        // 4. 发送请求
        await addProject(submitData)
        ElMessage.success('项目申报成功')
        router.push('/')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  fileList.value = []
}
</script>

<style scoped>
.project-form-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

