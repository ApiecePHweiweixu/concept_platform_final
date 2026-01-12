<template>
  <div class="page-wrap mentor-projects">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的辅导项目</h2>
        <p class="page-subtitle">作为导师查看分配到的孵化项目及里程碑进展</p>
      </div>
      <el-button type="primary" plain size="small" @click="refresh">刷新</el-button>
    </div>

    <el-card class="surface-card" shadow="never">
      <template #header>
        <div class="section-heading">
          <span>辅导项目列表</span>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" style="width: 100%" stripe class="tech-table">
        <el-table-column prop="projectId" label="项目ID" width="90" />
        <el-table-column prop="projectName" label="项目名称" min-width="180" />
        <el-table-column prop="status" label="孵化状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="openMilestoneDrawer(scope.row)">
              查看里程碑
            </el-button>
            <el-button type="success" link size="small" @click="openResourceDrawer(scope.row)">
              资源申请
            </el-button>
            <el-button
              v-if="scope.row.completionStatus === 2"
              type="warning"
              link
              size="small"
              @click="openCompletionReviewDialog(scope.row)"
            >
              落成终审
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 里程碑与报告审核 -->
    <el-drawer v-model="milestoneDrawerVisible" title="里程碑与报告" size="520px">
      <div v-if="currentIncubation">
        <p class="muted" style="margin-bottom: 8px;">项目ID：{{ currentIncubation.projectId }}</p>
        <el-timeline>
          <el-timeline-item
            v-for="m in milestones"
            :key="m.milestoneId"
            :timestamp="m.deadline ? m.deadline.replace('T', ' ') : ''"
            :type="getMilestoneType(m.status)"
          >
            <h4>{{ m.name }}（{{ getMilestoneLabel(m.status) }}）</h4>
            <p>{{ m.description }}</p>
            <p class="muted small">交付物：{{ m.deliverables }}</p>
            <el-button
              v-if="m.status === 2"
              type="primary"
              link
              size="small"
              @click="openExpertScoreDialog(m)"
            >
              专家打分
            </el-button>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>

    <el-dialog v-model="reviewVisible" :title="reviewForm.isExpertScore ? '专家打分' : '里程碑报告审核'" width="620px">
      <el-form :model="reviewForm" label-width="100px" class="tech-form">
        <el-form-item label="里程碑">
          <el-input v-model="reviewForm.milestoneName" disabled />
        </el-form-item>
        <el-form-item v-if="reviewForm.isExpertScore" label="评分(0-100)">
          <el-input-number
            v-model="reviewForm.score"
            :min="0"
            :max="100"
            placeholder="请输入0-100之间的分数"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-else label="是否通过">
          <el-switch v-model="reviewForm.pass" active-text="通过" inactive-text="需修订" />
        </el-form-item>
        <el-form-item label="反馈意见">
          <el-input
            v-model="reviewForm.feedback"
            type="textarea"
            rows="4"
            :placeholder="reviewForm.isExpertScore ? '请给出评分理由和改进建议' : '请给出针对性的改进建议'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingReview" @click="submitReview">
          {{ reviewForm.isExpertScore ? '提交评分' : '提交审核' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 资源申请侧边栏 -->
    <el-drawer v-model="resourceDrawerVisible" title="资源申请管理" size="600px">
      <el-table :data="resourceRequests" v-loading="resourceLoading" style="width: 100%" border>
        <el-table-column prop="projectName" label="项目名称" min-width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag size="small">{{ getResourceTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getResourceStatusType(scope.row.status)" size="small">
              {{ getResourceStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="160">
          <template #default="scope">
            {{ scope.row.createdAt ? scope.row.createdAt.replace('T', ' ').substring(0, 16) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0 || scope.row.status === 1"
              type="primary"
              link
              size="small"
              @click="openHandleResourceDialog(scope.row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <!-- 处理资源申请对话框 -->
    <el-dialog v-model="handleResourceVisible" title="处理资源申请" width="600px">
      <el-form :model="handleResourceForm" label-width="100px" class="tech-form">
        <el-form-item label="项目名称">
          <el-input v-model="handleResourceForm.projectName" disabled />
        </el-form-item>
        <el-form-item label="申请类型">
          <el-input v-model="handleResourceForm.typeLabel" disabled />
        </el-form-item>
        <el-form-item label="申请标题">
          <el-input v-model="handleResourceForm.title" disabled />
        </el-form-item>
        <el-form-item label="申请详情">
          <el-input v-model="handleResourceForm.description" type="textarea" rows="4" disabled />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="handleResourceForm.status" placeholder="请选择">
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </el-form-item>

        <!-- 实体资源分配逻辑 -->
        <template v-if="handleResourceForm.status === 2">
          <el-divider content-position="left">实物资源分配</el-divider>
          <el-form-item label="分配资源库">
            <el-select v-model="handleResourceForm.allocatedResourceId" placeholder="从可用库存中选择" style="width: 100%">
              <el-option
                v-for="item in matchingInventory"
                :key="item.resourceId"
                :label="`${item.resourceName} (余量: ${item.remainingQuota}${item.unit})`"
                :value="item.resourceId"
              />
            </el-select>
            <div v-if="matchingInventory.length === 0" class="text-danger small">该类型暂无可用库存资源</div>
          </el-form-item>
          <el-form-item label="分配数量">
            <el-input-number v-model="handleResourceForm.allocatedAmount" :min="0" style="width: 100%" />
          </el-form-item>
        </template>

        <el-form-item label="处理说明">
          <el-input
            v-model="handleResourceForm.historyJson"
            type="textarea"
            rows="3"
            placeholder="请填写处理说明或资源分配情况"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleResourceVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingHandleResource" @click="submitHandleResource">提交</el-button>
      </template>
    </el-dialog>

    <!-- 落成终审对话框 -->
    <el-dialog v-model="completionReviewVisible" title="落成终审打分" width="620px">
      <el-form :model="completionReviewForm" label-width="100px" class="tech-form">
        <el-form-item label="项目名称">
          <el-input v-model="completionReviewForm.projectName" disabled />
        </el-form-item>
        <el-form-item label="落成说明">
          <el-input v-model="completionReviewForm.completionDesc" type="textarea" rows="4" disabled />
        </el-form-item>
        <el-form-item label="材料包URL">
          <el-input v-model="completionReviewForm.completionPackageUrl" disabled />
        </el-form-item>
        <el-form-item label="终审评分(0-100)">
          <el-input-number
            v-model="completionReviewForm.score"
            :min="0"
            :max="100"
            placeholder="请输入0-100之间的分数"
            style="width: 100%"
          />
          <p class="muted small" style="margin-top: 4px;">评分≥60分视为通过，将自动纳入成功案例库</p>
        </el-form-item>
        <el-form-item label="终审反馈">
          <el-input
            v-model="completionReviewForm.feedback"
            type="textarea"
            rows="4"
            placeholder="请给出终审意见和改进建议"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completionReviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingCompletionReview" @click="submitCompletionReview">提交终审</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMentorIncubationProjects, getMilestones, getMilestoneReports, reviewMilestoneReport, getMentorResourceList, handleResource, expertScoreMilestoneReport, expertReviewCompletion, matchInventory } from '@/api/incubation'

const tableData = ref([])
const loading = ref(false)

const milestoneDrawerVisible = ref(false)
const currentIncubation = ref(null)
const milestones = ref([])

const reviewVisible = ref(false)
const reviewForm = ref({
  milestoneId: null,
  milestoneName: '',
  pass: true,
  feedback: '',
  score: null,
  isExpertScore: false // 是否为专家打分模式
})
const savingReview = ref(false)

const resourceDrawerVisible = ref(false)
const resourceRequests = ref([])
const resourceLoading = ref(false)
const currentProjectForResource = ref(null)

const handleResourceVisible = ref(false)
const matchingInventory = ref([])
const handleResourceForm = ref({
  requestId: null,
  projectName: '',
  type: '', // 原始类型编码
  typeLabel: '',
  title: '',
  description: '',
  status: 1,
  historyJson: '',
  allocatedResourceId: null,
  allocatedAmount: 0
})
const savingHandleResource = ref(false)

const completionReviewVisible = ref(false)
const completionReviewForm = ref({
  incubationId: null,
  projectName: '',
  completionDesc: '',
  completionPackageUrl: '',
  score: null,
  feedback: ''
})
const savingCompletionReview = ref(false)

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    0: '待签署协议',
    1: '孵化中',
    2: '已落成',
    3: '已终止'
  }
  return map[status] || '未知'
}

const getMilestoneType = (status) => {
  const map = {
    0: 'info',
    1: 'primary',
    2: 'warning',
    3: 'success',
    4: 'danger'
  }
  return map[status] || 'info'
}

const getMilestoneLabel = (status) => {
  const map = {
    0: '未开始',
    1: '进行中',
    2: '待审核',
    3: '已通过',
    4: '需修订'
  }
  return map[status] || '未知'
}

const refresh = async () => {
  loading.value = true
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      tableData.value = []
      return
    }
    const user = JSON.parse(userStr)
    const mentorId = user.userId || user.id || user.user_id
    const res = await getMentorIncubationProjects({ mentorId })
    tableData.value = Array.isArray(res) ? res : (res?.list || [])
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openMilestoneDrawer = async (row) => {
  currentIncubation.value = row
  milestoneDrawerVisible.value = true
  try {
    const res = await getMilestones(row.incubationId)
    milestones.value = Array.isArray(res) ? res : (res?.list || [])
  } catch (e) {
    console.error(e)
  }
}

const openExpertScoreDialog = async (m) => {
  // 先检查报告是否存在以及管理员是否已审批
  try {
    const reports = await getMilestoneReports(m.milestoneId)
    const list = Array.isArray(reports) ? reports : (reports?.list || [])
    if (!list.length) {
      ElMessage.warning('该里程碑尚未提交报告，无法打分')
      return
    }
    const latestReport = list[0]
    if (!latestReport.adminApproved || latestReport.adminApproved !== 1) {
      ElMessage.warning('该报告尚未通过管理员审批，无法打分')
      return
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取报告信息失败')
    return
  }

  reviewForm.value = {
    milestoneId: m.milestoneId,
    milestoneName: m.name,
    pass: true,
    feedback: '',
    score: null,
    isExpertScore: true
  }
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.milestoneId) {
    ElMessage.warning('里程碑ID缺失，无法操作')
    return
  }

  if (reviewForm.value.isExpertScore) {
    // 专家打分
    if (reviewForm.value.score === null || reviewForm.value.score < 0 || reviewForm.value.score > 100) {
      ElMessage.warning('请输入0-100之间的有效分数')
      return
    }
    savingReview.value = true
    try {
      await expertScoreMilestoneReport({
        milestoneId: reviewForm.value.milestoneId,
        score: reviewForm.value.score,
        feedback: reviewForm.value.feedback
      })
      ElMessage.success('评分已提交')
      reviewVisible.value = false
      // 刷新里程碑列表
      if (currentIncubation.value) {
        const res = await getMilestones(currentIncubation.value.incubationId)
        milestones.value = Array.isArray(res) ? res : (res?.list || [])
      }
    } catch (e) {
      console.error(e)
      ElMessage.error(e.message || '评分提交失败')
    } finally {
      savingReview.value = false
    }
  } else {
    // 原有的审核逻辑（保留用于其他场景）
    savingReview.value = true
    try {
      await reviewMilestoneReport(reviewForm.value)
      ElMessage.success('审核已提交')
      reviewVisible.value = false
    } catch (e) {
      console.error(e)
    } finally {
      savingReview.value = false
    }
  }
}

const openResourceDrawer = async (row) => {
  currentProjectForResource.value = row
  resourceDrawerVisible.value = true
  resourceRequests.value = [] // 清空之前的数据
  await loadResourceRequests()
}

const loadResourceRequests = async () => {
  resourceLoading.value = true
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      resourceLoading.value = false
      return
    }
    const user = JSON.parse(userStr)
    const mentorId = user.userId || user.id || user.user_id
    if (!mentorId) {
      ElMessage.warning('无法获取用户ID')
      resourceLoading.value = false
      return
    }
    const res = await getMentorResourceList({ mentorId })
    const list = Array.isArray(res) ? res : (res?.list || [])
    // 只显示当前项目的资源申请
    if (currentProjectForResource.value) {
      resourceRequests.value = list.filter(req => 
        req.projectId === currentProjectForResource.value.projectId ||
        req.incubationId === currentProjectForResource.value.incubationId
      )
    } else {
      resourceRequests.value = list
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载资源申请失败：' + (e.message || '未知错误'))
  } finally {
    resourceLoading.value = false
  }
}

const getResourceTypeLabel = (type) => {
  const map = {
    'TECH': '技术咨询',
    'IP': '知识产权辅导',
    'MARKET': '市场渠道对接',
    'FUND': '融资路演机会',
    'FUNDING': '拨款支持',
    'SERVER': '服务器/算力',
    'SUPPORT': '技术支持',
    'INTERNSHIP': '企业实习',
    'MENTOR': '辅导/陪跑'
  }
  return map[type] || type
}

const getResourceStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getResourceStatusLabel = (status) => {
  const map = {
    0: '待处理',
    1: '处理中',
    2: '已完成',
    3: '已拒绝'
  }
  return map[status] || '未知'
}

const openHandleResourceDialog = async (row) => {
  handleResourceForm.value = {
    requestId: row.requestId,
    projectName: row.projectName,
    type: row.type,
    typeLabel: getResourceTypeLabel(row.type),
    title: row.title,
    description: row.description,
    status: row.status || 1,
    historyJson: row.historyJson || '',
    allocatedResourceId: null,
    allocatedAmount: 0
  }
  handleResourceVisible.value = true

  // 异步加载匹配的库存资源
  try {
    const res = await matchInventory(row.type)
    matchingInventory.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('加载匹配资源失败', e)
  }
}

const submitHandleResource = async () => {
  if (!handleResourceForm.value.requestId) {
    ElMessage.warning('请求ID缺失')
    return
  }
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.error('未登录')
    return
  }
  const user = JSON.parse(userStr)
  const handlerId = user.userId || user.id || user.user_id
  
  savingHandleResource.value = true
  try {
    await handleResource({
      requestId: handleResourceForm.value.requestId,
      status: handleResourceForm.value.status,
      handlerId,
      historyJson: handleResourceForm.value.historyJson,
      allocatedResourceId: handleResourceForm.value.allocatedResourceId,
      allocatedAmount: handleResourceForm.value.allocatedAmount
    })
    ElMessage.success('处理成功')
    handleResourceVisible.value = false
    await loadResourceRequests()
  } catch (e) {
    console.error(e)
  } finally {
    savingHandleResource.value = false
  }
}

const openCompletionReviewDialog = (row) => {
  completionReviewForm.value = {
    incubationId: row.incubationId || row.projectId,
    projectName: row.projectName || '',
    completionDesc: row.completionDesc || '',
    completionPackageUrl: row.completionPackageUrl || '',
    score: null,
    feedback: ''
  }
  completionReviewVisible.value = true
}

const submitCompletionReview = async () => {
  if (!completionReviewForm.value.incubationId) {
    ElMessage.warning('孵化项目ID不能为空')
    return
  }
  if (completionReviewForm.value.score === null || completionReviewForm.value.score < 0 || completionReviewForm.value.score > 100) {
    ElMessage.warning('请填写0-100之间的有效评分')
    return
  }
  savingCompletionReview.value = true
  try {
    await expertReviewCompletion({
      incubationId: completionReviewForm.value.incubationId,
      score: completionReviewForm.value.score,
      feedback: completionReviewForm.value.feedback
    })
    const pass = completionReviewForm.value.score >= 60
    ElMessage.success(pass ? '终审通过，项目已纳入成功案例库' : '终审拒绝，评分不足60分')
    completionReviewVisible.value = false
    refresh()
  } catch (e) {
    console.error(e)
    ElMessage.error('终审失败：' + (e.message || '未知错误'))
  } finally {
    savingCompletionReview.value = false
  }
}

refresh()
</script>

<style scoped>
.mentor-projects {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.small {
  font-size: 12px;
}
</style>



