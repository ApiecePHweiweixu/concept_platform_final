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
            <el-table-column prop="techDomain" label="技术领域" width="150">
              <template #default="scope">
                {{ formatTechDomain(scope.row.techDomain) }}
              </template>
            </el-table-column>
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
            <el-table-column prop="techDomain" label="技术领域" width="150">
              <template #default="scope">
                {{ formatTechDomain(scope.row.techDomain) }}
              </template>
            </el-table-column>
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
        <el-descriptions-item label="技术领域">{{ formatTechDomain(currentProject.techDomain) }}</el-descriptions-item>
        <el-descriptions-item label="项目简介">{{ currentProject.description }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />

      <!-- AI 辅助意见 -->
      <div v-if="aiAnalysis" class="ai-analysis-box mb-4">
        <div class="ai-header" @click="showAiDetails = !showAiDetails">
          <div class="ai-title">
            <el-icon class="ai-icon"><Cpu /></el-icon>
            <span>AI 辅助初筛意见</span>
            <el-tag v-if="aiAnalysis.status === 0" type="warning" size="small" class="ml-2">分析中...</el-tag>
            <el-tag v-else-if="aiAnalysis.status === 1" type="success" size="small" class="ml-2">已完成</el-tag>
          </div>
          <el-icon><ArrowDown v-if="!showAiDetails" /><ArrowUp v-else /></el-icon>
        </div>
        
        <el-collapse-transition>
          <div v-show="showAiDetails" class="ai-content">
            <template v-if="aiAnalysis.status === 1">
              <div class="ai-scores">
                <div class="score-item">
                  <span class="label">创新性</span>
                  <el-progress type="circle" :percentage="aiAnalysis.innovationScore" :width="50" :stroke-width="4" />
                </div>
                <div class="score-item">
                  <span class="label">可行性</span>
                  <el-progress type="circle" :percentage="aiAnalysis.feasibilityScore" :width="50" :stroke-width="4" />
                </div>
                <div class="score-item">
                  <span class="label">市场潜力</span>
                  <el-progress type="circle" :percentage="aiAnalysis.marketScore" :width="50" :stroke-width="4" />
                </div>
              </div>
              <div class="ai-summary">
                <strong>综合评价：</strong>{{ aiAnalysis.analysisSummary }}
              </div>
              <div class="ai-risks mt-2">
                <strong>风险提示：</strong><span class="text-danger">{{ aiAnalysis.riskWarning }}</span>
              </div>
            </template>
            <template v-else-if="aiAnalysis.status === 0">
              <div class="p-4 text-center muted">AI 正在努力分析中，请稍后重试或刷新...</div>
            </template>
            <template v-else>
              <div class="p-4 text-center text-danger">AI 分析失败，请检查 API 配置或重试。</div>
            </template>
          </div>
        </el-collapse-transition>
      </div>

      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px" style="margin-top: 10px;" class="tech-form">
        <el-form-item label="可行性" prop="feasibilityScore">
          <el-input-number v-model="form.feasibilityScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="深度" prop="depthScore">
          <el-input-number v-model="form.depthScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="拓展度" prop="extensionScore">
          <el-input-number v-model="form.extensionScore" :min="0" :max="100" />
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
import { getAiAnalysis } from '@/api/project'
import { ElMessage } from 'element-plus'
import { formatTechDomain } from '@/utils/format'
import { Cpu, ArrowDown, ArrowUp } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const activeTab = ref('pending')
const allReviews = ref([])
const currentProject = ref({})
const formRef = ref(null)

// AI 分析相关
const aiAnalysis = ref(null)
const showAiDetails = ref(true)

const form = reactive({
  reviewId: null,
  projectId: null,
  feasibilityScore: 80,
  depthScore: 80,
  extensionScore: 80,
  comment: ''
})

const rules = {
  feasibilityScore: [{ required: true, message: '请输入可行性评分', trigger: 'blur' }],
  depthScore: [{ required: true, message: '请输入深度评分', trigger: 'blur' }],
  extensionScore: [{ required: true, message: '请输入拓展度评分', trigger: 'blur' }],
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

const handleReview = async (row) => {
  currentProject.value = row
  form.reviewId = row.reviewId || row.id 
  form.projectId = row.projectId
  currentProject.value.description = row.description || row.projectDescription || '暂无简介'
  form.feasibilityScore = 80
  form.depthScore = 80
  form.extensionScore = 80
  form.comment = ''
  dialogVisible.value = true

  // 加载 AI 分析结果
  aiAnalysis.value = null
  try {
    const res = await getAiAnalysis(row.projectId)
    aiAnalysis.value = res
  } catch (e) {
    console.error('Failed to load AI analysis', e)
  }
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
        feasibilityScore: form.feasibilityScore,
        depthScore: form.depthScore,
        extensionScore: form.extensionScore,
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

/* AI Analysis Box Styles */
.ai-analysis-box {
  border: 1px solid var(--el-color-primary-light-7);
  border-radius: 8px;
  background-color: var(--el-color-primary-light-9);
  overflow: hidden;
  margin-top: 10px;
}

.ai-header {
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  background-color: var(--el-color-primary-light-8);
  transition: background-color 0.3s;
}

.ai-header:hover {
  background-color: var(--el-color-primary-light-7);
}

.ai-title {
  display: flex;
  align-items: center;
  font-weight: bold;
  color: var(--el-color-primary);
}

.ai-icon {
  margin-right: 8px;
  font-size: 18px;
}

.ai-content {
  padding: 15px;
  border-top: 1px solid var(--el-color-primary-light-7);
}

.ai-scores {
  display: flex;
  justify-content: space-around;
  margin-bottom: 15px;
}

.score-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.score-item .label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.ai-summary {
  font-size: 14px;
  line-height: 1.6;
  color: var(--el-text-color-primary);
}

.ai-risks {
  font-size: 13px;
  padding: 8px 12px;
  background-color: #fff;
  border-radius: 4px;
  border-left: 4px solid var(--el-color-danger);
}
</style>
