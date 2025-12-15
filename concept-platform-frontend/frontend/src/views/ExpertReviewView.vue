<template>
  <div class="expert-reviews-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评审任务</span>
          <el-button type="primary" size="small" @click="fetchData">刷新</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="review-tabs">
        <!-- 待评审 Tab -->
        <el-tab-pane label="待评审" name="pending">
          <el-table :data="pendingList" v-loading="loading" style="width: 100%" stripe>
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
                <span v-else style="color: #909399; font-size: 12px;">无附件</span>
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

        <!-- 已评审 Tab -->
        <el-tab-pane label="已评审" name="reviewed">
          <el-table :data="reviewedList" v-loading="loading" style="width: 100%" stripe>
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

    <!-- 评审弹窗 -->
    <el-dialog v-model="dialogVisible" title="项目评审" width="600px" @close="resetForm">
      <el-descriptions title="项目信息" :column="1" border>
        <el-descriptions-item label="项目名称">{{ currentProject.projectName }}</el-descriptions-item>
        <el-descriptions-item label="技术领域">{{ currentProject.techDomain }}</el-descriptions-item>
        <el-descriptions-item label="项目简介">{{ currentProject.description }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider />

      <el-form :model="form" ref="formRef" :rules="rules" label-width="80px" style="margin-top: 20px;">
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
  reviewId: null, // 评审任务ID
  projectId: null,
  score: 80,
  comment: ''
})

const rules = {
  score: [{ required: true, message: '请输入评分', trigger: 'blur' }],
  comment: [{ required: true, message: '请输入评语', trigger: 'blur' }]
}

// 拆分数据
const pendingList = computed(() => {
  return allReviews.value.filter(item => item.status === 0 || !item.status) // status 0 或 null 为待评审
})

const reviewedList = computed(() => {
  return allReviews.value.filter(item => item.status === 1) // status 1 为已评审
})

const fetchData = async () => {
  loading.value = true
  try {
    const userStr = localStorage.getItem('user')
    let userId = null
    if (userStr) {
      const user = JSON.parse(userStr)
      // 兼容多种 ID 字段名
      userId = user.userId || user.user_id || user.id
    }
    
    // 传入 expertId 获取任务列表
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
  // 关键修复：正确获取评审任务 ID
  // 优先取 row.reviewId，如果没有则尝试取 row.id
  form.reviewId = row.reviewId || row.id 
  form.projectId = row.projectId
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
      // 调试：打印提交数据
      console.log('提交评审数据:', form)

      submitting.value = true
      try {
        await submitReview(form)
        ElMessage.success('评审提交成功')
        dialogVisible.value = false
        fetchData() // 刷新列表
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
.expert-reviews-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.review-tabs {
  margin-top: 10px;
}
</style>

