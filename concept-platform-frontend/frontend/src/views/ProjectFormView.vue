<template>
  <div class="page-wrap project-form">
    <div class="page-header">
      <div>
        <h2 class="page-title">项目申报</h2>
        <p class="page-subtitle">提交项目信息与附件，进入审核流程</p>
      </div>
      <el-button plain @click="$router.back()">返回</el-button>
    </div>

    <el-card class="surface-card" shadow="never">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px" class="tech-form">
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
              <div class="el-upload__tip muted">
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
  attachmentUrl: ''
})

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  techDomain: [{ required: true, message: '请输入技术领域', trigger: 'blur' }],
  attachmentUrl: [{ required: true, message: '请上传附件', trigger: 'change' }]
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.attachmentUrl = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
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
        const userStr = localStorage.getItem('user')
        let user = {}
        if (userStr) {
          try {
            user = JSON.parse(userStr)
          } catch (e) {
            console.error('JSON Parse Error', e)
          }
        }

        const submitData = {
          projectName: form.projectName,
          description: form.description,
          techDomain: form.techDomain,
          attachmentUrl: form.attachmentUrl,
          status: 1
        }

        const userId = user.userId || user.user_id || user.id
        submitData.applicantId = userId || 1
        if (!userId) {
          ElMessage.warning('当前未读取到用户ID，使用测试ID: 1')
        }

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
.project-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 900px;
}
</style>
