<template>
  <div class="page-wrap expert-reviews">
    <div class="page-header">
      <div>
        <h2 class="page-title">评审任务</h2>
        <p class="page-subtitle">处理待评审项目并查看历史评审</p>
      </div>
      <el-button type="primary" plain size="small" @click="fetchData">刷新</el-button>
    </div>

    <el-card class="surface-card" shadow="never">
      <el-tabs v-model="activeTab" class="review-tabs">
        <el-tab-pane label="待评审" name="pending">
          <el-table :data="pendingList" v-loading="loading" style="width: 100%" stripe class="tech-table">
            <el-table-column prop="projectName" label="项目名称" min-width="150" />
            <el-table-column label="申报材料" width="120">
              <template #default="scope">
                <el-link 
                  v-if="scope.row.attachmentUrl" 
                  :href="getFullUrl(scope.row.attachmentUrl)" 
                  target="_blank" 
                  type="primary"
                >
                  查看附件
                </el-link>
                <span v-else class="muted small">无附件</span>
              </template>
            </el-table-column>
            <el-table-column prop="techDomain" label="技术领域" width="150" />
            <el-table-column prop="deadline" label="截止时间" width="180">
               <template #default="scope">
                {{ scope.row.deadline ? scope.row.deadline.replace('T', ' ') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleReview(scope.row)">评审</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已评审" name="reviewed">
          <el-table :data="reviewedList" v-loading="loading" style="width: 100%" stripe class="tech-table">
            <el-table-column prop="projectName" label="项目名称" min-width="150" />
            <el-table-column prop="techDomain" label="技术领域" width="150" />
            <el-table-column prop="score" label="评分" width="100" />
            <el-table-column prop="reviewTime" label="评审时间" width="180">
              <template #default="scope">
                {{ scope.row.reviewTime ? scope.row.reviewTime.replace('T', ' ').substring(0, 19) : '-' }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" title="项目评审" width="620px" @close="resetForm">
      <el-descriptions title="项目信息" :column="1" border>
        <el-descriptions-item label="项目名称">{{ currentProject.projectName }}</el-descriptions-item>
        <el-descriptions-item label="技术领域">{{ currentProject.techDomain }}</el-descriptions-item>
        <el-descriptions-item label="项目简介">{{ currentProject.description }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <el-form :model="form" ref="formRef" :rules="rules" label-width="80px" style="margin-top: 10px;" class="tech-form">
        <el-form-item label="评分" prop="score">
          <el-input-number v-model="form.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="评语" prop="comment">
          <el-input v-model="form.comment" type="textarea" rows="4" placeholder="请输入评语" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit" :loading="submitting">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getReviewList, submitReview } from '@/api/review'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const activeTab = ref('pending')
const allReviews = ref([])
const currentProject = ref({})
const formRef = ref(null)

const form = reactive({
  reviewId: null,
  projectId: null,
  score: 80,
  comment: ''
})

const rules = {
  score: [{ required: true, message: '请输入评分', trigger: 'blur' }],
  comment: [{ required: true, message: '请输入评语', trigger: 'blur' }]
}

const pendingList = computed(() => {
  return allReviews.value.filter(item => item.status === 0 || !item.status)
})

const reviewedList = computed(() => {
  return allReviews.value.filter(item => item.status === 1)
})

const fetchData = async () => {
  loading.value = true
  try {
    const userStr = localStorage.getItem('user')
    let userId = null
    if (userStr) {
      const user = JSON.parse(userStr)
      userId = user.userId || user.user_id || user.id
    }
    const res = await getReviewList({ expertId: userId })
    if (Array.isArray(res)) {
      allReviews.value = res
    } else if (res && Array.isArray(res.list)) {
      allReviews.value = res.list
    } else {
      allReviews.value = []
    }
  } catch (error) {
    console.error('获取评审列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleReview = (row) => {
  currentProject.value = row
  form.reviewId = row.reviewId || row.id 
  form.projectId = row.projectId
  currentProject.value.description = row.description || row.projectDescription || '暂无简介'
  form.score = 80
  form.comment = ''
  dialogVisible.value = true
}

const getFullUrl = (url) => {
  if (!url) return ''
  if (url.match(/^https?:\/\//)) return url
  return `http://localhost:8080${url}`
}

const resetForm = () => {
  formRef.value?.resetFields()
  currentProject.value = {}
}

const submit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      const submitData = {
        reviewId: form.reviewId,
        projectId: form.projectId,
        score: form.score,
        comments: form.comment
      }

      submitting.value = true
      try {
        await submitReview(submitData)
        ElMessage.success('评审提交成功')
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.expert-reviews {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-tabs {
  margin-top: 6px;
}

.muted.small {
  font-size: 12px;
}
</style>
