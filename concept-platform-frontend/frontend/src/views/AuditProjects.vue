<template>
  <div class="page-wrap audit-projects">
    <div class="page-header">
      <div>
        <h2 class="page-title">项目审核</h2>
        <p class="page-subtitle">按状态查看待审与已处理项目</p>
      </div>
      <el-button type="primary" plain size="small" @click="fetchData">刷新</el-button>
    </div>

    <el-card class="surface-card" shadow="never">
      <el-tabs v-model="activeTab" class="audit-tabs">
        <el-tab-pane label="待审核" name="pending">
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
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="scope">
                {{ scope.row.createdAt ? scope.row.createdAt.replace('T', ' ') : '' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleAudit(scope.row)">审核</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已审核" name="processed">
          <el-table :data="processedList" v-loading="loading" style="width: 100%" stripe class="tech-table">
            <el-table-column prop="projectName" label="项目名称" min-width="150" />
            <el-table-column prop="techDomain" label="技术领域" width="150">
              <template #default="scope">
                {{ formatTechDomain(scope.row.techDomain) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="当前状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="finalScore" label="评审得分" width="100">
              <template #default="scope">
                <span v-if="scope.row.status === 3 || scope.row.status === 9" :class="scope.row.finalScore >= 60 ? 'text-success' : 'text-danger'">
                  {{ scope.row.finalScore || '-' }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="auditTime" label="审核时间" width="180">
              <template #default="scope">
                {{ scope.row.auditTime ? scope.row.auditTime.replace('T', ' ') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
               <template #default="scope">
                  <el-button link type="primary" size="small" @click="handleView(scope.row)">查看详情</el-button>
               </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" title="项目审核" width="520px" @close="resetForm">
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

      <el-form :model="auditForm" label-width="80px" class="tech-form">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.action">
            <el-radio value="PASS">通过并指派</el-radio>
            <el-radio value="REJECT">驳回</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="auditForm.action === 'PASS'">
          <el-form-item label="项目领域">
            <el-tag type="info" size="small">{{ formatTechDomain(auditForm.projectTechDomain) }}</el-tag>
          </el-form-item>
          <el-form-item label="指派专家" required>
            <el-checkbox-group v-model="auditForm.expertIds">
              <el-checkbox v-for="item in filteredExpertList" :key="item.userId" :label="item.userId">
                {{ item.realName || item.username }} ({{ item.field || '通用' }})
              </el-checkbox>
            </el-checkbox-group>
            <div v-if="filteredExpertList.length === 0" class="text-danger small">该领域暂无匹配专家，请先在用户管理中添加相关领域的专家。</div>
            <div v-else class="muted small">已自动筛选 [{{ formatTechDomain(auditForm.projectTechDomain) }}] 领域的专家。请选择 1-3 名。</div>
          </el-form-item>
        </template>

        <template v-if="auditForm.action === 'REJECT'">
          <el-form-item label="驳回理由" required>
            <el-input v-model="auditForm.comment" type="textarea" rows="3" placeholder="请输入驳回理由" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="项目详情" width="620px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="项目名称">{{ currentDetail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="技术领域">{{ formatTechDomain(currentDetail.techDomain) }}</el-descriptions-item>
        <el-descriptions-item label="申报人">{{ currentDetail.applicantName || currentDetail.applicantId }}</el-descriptions-item>
        <el-descriptions-item label="项目简介">{{ currentDetail.description }}</el-descriptions-item>
        <el-descriptions-item label="附件">
          <el-link 
            v-if="currentDetail.attachmentUrl" 
            :href="getFullUrl(currentDetail.attachmentUrl)" 
            target="_blank" 
            type="primary"
          >
            下载申报书
          </el-link>
          <span v-else class="muted">无附件</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getList, auditProject, getAiAnalysis } from '@/api/project'
import { getExperts } from '@/api/user'
import { ElMessage } from 'element-plus'
import { formatTechDomain } from '@/utils/format'
import { Cpu, ArrowDown, ArrowUp } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const submitting = ref(false)
const expertList = ref([])
const activeTab = ref('pending')
const allProjects = ref([])
const currentDetail = ref({})

// AI 分析相关
const aiAnalysis = ref(null)
const showAiDetails = ref(true)

const auditForm = reactive({
  projectId: null,
  action: 'PASS',
  expertIds: [],
  comment: '',
  projectTechDomain: '' // 新增：记录当前审核项目的领域
})

// 计算属性：根据项目领域严格过滤专家列表
const filteredExpertList = computed(() => {
  if (!auditForm.projectTechDomain) return []
  
  // 提取项目的领域关键词
  let domains = []
  try {
    domains = JSON.parse(auditForm.projectTechDomain)
  } catch (e) {
    domains = auditForm.projectTechDomain.split(/[,/、]/).filter(s => s.trim())
  }
  
  // 仅保留领域匹配的专家
  return expertList.value.filter(expert => {
    if (!expert.field) return false
    return domains.some(d => expert.field.includes(d) || d.includes(expert.field))
  })
})

const pendingList = computed(() => {
  return allProjects.value.filter(item => item.status === 1)
})

const processedList = computed(() => {
  return allProjects.value.filter(item => [2, 3, 9].includes(item.status))
})

const getStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'primary',
    2: 'warning',
    3: 'success',
    9: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    0: '草稿',
    1: '待初审',
    2: '评审中',
    3: '已入库',
    9: '已驳回'
  }
  return map[status] || '未知状态'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getList({})
    if (Array.isArray(res)) {
      allProjects.value = res
    } else if (res && Array.isArray(res.list)) {
      allProjects.value = res.list
    } else {
      allProjects.value = []
    }
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchExperts = async () => {
  try {
    const res = await getExperts()
    const list = Array.isArray(res) ? res : (res.list || [])
    expertList.value = list.map(item => ({
      ...item,
      userId: item.userId || item.id
    }))
  } catch (error) {
    console.error('获取专家列表失败', error)
  }
}

const getFullUrl = (url) => {
  if (!url) return ''
  if (url.match(/^https?:\/\//)) return url
  return `http://localhost:8080${url}`
}

const handleAudit = async (row) => {
  const projectId = row.id || row.projectId
  auditForm.projectId = projectId
  auditForm.projectTechDomain = row.techDomain || ''
  auditForm.action = 'PASS'
  auditForm.expertIds = []
  auditForm.comment = ''
  dialogVisible.value = true
  
  // 加载 AI 分析结果
  aiAnalysis.value = null
  try {
    const res = await getAiAnalysis(projectId)
    aiAnalysis.value = res
  } catch (e) {
    console.error('Failed to load AI analysis', e)
  }
}

const handleView = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

const resetForm = () => {
  auditForm.projectId = null
  auditForm.action = 'PASS'
  auditForm.expertIds = []
  auditForm.comment = ''
}

const submitAudit = async () => {
  let payload = {}

  if (auditForm.action === 'PASS') {
    if (auditForm.expertIds.length < 1 || auditForm.expertIds.length > 3) {
      ElMessage.warning('请选择 1-3 名专家')
      return
    }
    payload = {
      projectId: auditForm.projectId,
      pass: true,
      expertIds: auditForm.expertIds,
      rejectReason: ''
    }
  } else if (auditForm.action === 'REJECT') {
    if (!auditForm.comment.trim()) {
      ElMessage.warning('请输入驳回理由')
      return
    }
    payload = {
      projectId: auditForm.projectId,
      pass: false,
      rejectReason: auditForm.comment,
      expertIds: []
    }
  }

  submitting.value = true
  try {
    await auditProject(payload)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    await fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchExperts()
})
</script>

<style scoped>
.audit-projects {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.audit-tabs {
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
  margin-bottom: 20px;
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
