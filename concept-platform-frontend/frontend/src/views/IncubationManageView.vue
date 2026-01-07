<template>
  <div class="page-wrap incubation-manage">
    <div class="page-header">
      <div>
        <h2 class="page-title">孵化项目管理</h2>
        <p class="page-subtitle">平台管理员视角：准予入孵、指派项目经理与导师、查看孵化进展</p>
      </div>
      <el-button type="primary" plain size="small" @click="refresh">刷新</el-button>
    </div>

    <el-card class="surface-card" shadow="never">
      <template #header>
        <div class="section-heading">
          <span>孵化项目列表</span>
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
        <el-table-column prop="projectManagerId" label="项目经理ID" width="120" />
        <el-table-column label="操作" width="420" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 4"
              type="primary"
              link
              size="small"
              @click="handleApproveIncubation(scope.row)"
            >
              准予入孵
            </el-button>
            <el-button
              type="primary"
              link
              size="small"
              @click="openAssignDialog(scope.row)"
            >
              指派经理与导师
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="success"
              link
              size="small"
              @click="openMilestoneReviewDialog(scope.row)"
            >
              审批里程碑报告
            </el-button>
            <el-button
              v-if="scope.row.status === 1 && scope.row.completionStatus === 1"
              type="warning"
              link
              size="small"
              @click="openCompletionReviewDialog(scope.row)"
            >
              落成初审
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="assignVisible" title="指派项目经理与导师" width="620px">
      <el-form :model="assignForm" label-width="100px" class="tech-form">
        <el-form-item label="孵化项目ID">
          <el-input v-model="assignForm.incubationId" disabled />
        </el-form-item>
        <el-form-item label="项目经理ID">
          <el-input v-model="assignForm.projectManagerId" placeholder="可直接填管理员 userId" />
        </el-form-item>
        <el-form-item label="导师">
          <el-select
            v-model="assignForm.mentorIdList"
            multiple
            filterable
            placeholder="请选择导师（可多选）"
          >
            <el-option
              v-for="expert in expertList"
              :key="expert.userId"
              :label="expert.realName || expert.username || ('专家' + expert.userId)"
              :value="String(expert.userId)"
            />
          </el-select>
          <p class="muted small" style="margin-top: 4px;">
            将自动按所选导师的 userId 生成逗号分隔的 ID 列表，用于专家视角的“我的辅导项目”匹配。
          </p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingAssign" @click="submitAssign">保存</el-button>
      </template>
    </el-dialog>

    <!-- 里程碑报告审批对话框 -->
    <el-dialog v-model="milestoneReviewVisible" title="审批里程碑报告" width="720px">
      <el-form :model="milestoneReviewForm" label-width="120px" class="tech-form">
        <el-form-item label="孵化项目ID">
          <el-input v-model="milestoneReviewForm.incubationId" disabled />
        </el-form-item>
        <el-form-item label="选择里程碑">
          <el-select v-model="milestoneReviewForm.milestoneId" placeholder="请选择里程碑" @change="loadMilestoneReport">
            <el-option
              v-for="m in availableMilestones"
              :key="m.milestoneId"
              :label="m.name"
              :value="m.milestoneId"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="currentReport" label="报告内容">
          <el-input v-model="currentReport.content" type="textarea" rows="6" disabled />
        </el-form-item>
        <el-form-item v-if="currentReport" label="附件">
          <el-input v-model="currentReport.attachments" type="textarea" rows="2" disabled />
        </el-form-item>
        <el-form-item label="审批结果">
          <el-radio-group v-model="milestoneReviewForm.approved">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input
            v-model="milestoneReviewForm.feedback"
            type="textarea"
            rows="4"
            placeholder="请填写审批意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="milestoneReviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingMilestoneReview" @click="submitMilestoneReview">提交审批</el-button>
      </template>
    </el-dialog>

    <!-- 落成申请审批对话框 -->
    <el-dialog v-model="completionReviewVisible" title="落成申请审批" width="620px">
      <el-form :model="completionReviewForm" label-width="100px" class="tech-form">
        <el-form-item label="孵化项目ID">
          <el-input v-model="completionReviewForm.incubationId" disabled />
        </el-form-item>
        <el-form-item label="审批结果">
          <el-radio-group v-model="completionReviewForm.pass">
            <el-radio :label="true">通过（纳入成功案例）</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completionReviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingCompletionReview" @click="submitCompletionReview">提交审批</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { approveIncubation as approveIncubationApi, assignIncubation, getAdminIncubationList, getMilestones, getMilestoneReports, adminApproveMilestoneReport, adminReviewCompletion } from '@/api/incubation'
import { getExperts } from '@/api/user'

const tableData = ref([])
const loading = ref(false)

const assignVisible = ref(false)
const assignForm = ref({
  incubationId: null,
  projectManagerId: '',
  // 用于界面选择的导师ID数组
  mentorIdList: []
})
const savingAssign = ref(false)

// 专家列表（用于下拉选择导师）
const expertList = ref([])

const milestoneReviewVisible = ref(false)
const milestoneReviewForm = ref({
  incubationId: null,
  milestoneId: null,
  approved: true,
  feedback: ''
})
const availableMilestones = ref([])
const currentReport = ref(null)
const savingMilestoneReview = ref(false)

const completionReviewVisible = ref(false)
const completionReviewForm = ref({
  incubationId: null,
  pass: true
})
const savingCompletionReview = ref(false)

const getStatusType = (status) => {
  // 这里的 status 来自 incubation_project.status，而不是项目主表的 status
  // 0-待签署协议, 1-孵化中, 2-已落成, 3-终止
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  // 显示孵化项目自身的状态，而不是“待初审”等概念验证状态
  const map = {
    0: '待签署协议',
    1: '孵化中',
    2: '已落成',
    3: '已终止'
  }
  return map[status] || '未知'
}

// 加载专家列表，用于指派导师时按姓名选择
const fetchExperts = async () => {
  try {
    const res = await getExperts()
    const list = Array.isArray(res) ? res : (res?.list || [])
    expertList.value = list.map(item => ({
      ...item,
      userId: item.userId || item.id
    }))
  } catch (e) {
    console.error('获取专家列表失败', e)
  }
}

const refresh = async () => {
  loading.value = true
  try {
    const res = await getAdminIncubationList()
    tableData.value = Array.isArray(res) ? res : (res?.list || [])
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refresh()
  fetchExperts()
})

const handleApproveIncubation = async (row) => {
  try {
    await approveIncubationApi({ projectId: row.projectId })
    ElMessage.success('已标记为准予入孵')
    refresh()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败：' + (e.message || '未知错误'))
  }
}

const openAssignDialog = (row) => {
  // 这里只需要孵化项目ID，简化为与 projectId 相同的场景下由后端去匹配
  assignForm.value = {
    incubationId: row.incubationId || row.projectId || row.id,
    projectManagerId: row.projectManagerId || '',
    // 将字符串形式的 mentorIds 转为数组，便于多选回显
    mentorIdList: row.mentorIds
      ? String(row.mentorIds)
          .split(',')
          .map(id => id.trim())
          .filter(id => id !== '')
      : []
  }
  assignVisible.value = true
}

const submitAssign = async () => {
  if (!assignForm.value.incubationId) {
    ElMessage.warning('孵化项目ID不能为空')
    return
  }
  savingAssign.value = true
  try {
    // 将多选导师ID数组转换为后端需要的逗号分隔字符串
    const payload = {
      incubationId: assignForm.value.incubationId,
      projectManagerId: assignForm.value.projectManagerId || null,
      mentorIds: Array.isArray(assignForm.value.mentorIdList)
        ? assignForm.value.mentorIdList.join(',')
        : ''
    }
    await assignIncubation(payload)
    ElMessage.success('指派成功')
    assignVisible.value = false
    refresh()
  } catch (e) {
    console.error(e)
    ElMessage.error('指派失败：' + (e.message || '未知错误'))
  } finally {
    savingAssign.value = false
  }
}

const openMilestoneReviewDialog = async (row) => {
  milestoneReviewForm.value = {
    incubationId: row.incubationId || row.projectId,
    milestoneId: null,
    approved: true,
    feedback: ''
  }
  currentReport.value = null
  milestoneReviewVisible.value = true
  // 加载里程碑列表
  try {
    const res = await getMilestones(milestoneReviewForm.value.incubationId)
    availableMilestones.value = Array.isArray(res) ? res : (res?.list || [])
    // 只显示待审核的里程碑（status=2）
    availableMilestones.value = availableMilestones.value.filter(m => m.status === 2)
    if (availableMilestones.value.length === 0) {
      ElMessage.info('当前没有待审批的里程碑报告')
      milestoneReviewVisible.value = false
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载里程碑失败')
  }
}

const openCompletionReviewDialog = (row) => {
  completionReviewForm.value = {
    incubationId: row.incubationId || row.projectId,
    pass: true
  }
  completionReviewVisible.value = true
}

const submitCompletionReview = async () => {
  if (!completionReviewForm.value.incubationId) {
    ElMessage.warning('孵化项目ID不能为空')
    return
  }
  savingCompletionReview.value = true
  try {
    await adminReviewCompletion(completionReviewForm.value)
    ElMessage.success(completionReviewForm.value.pass ? '初审通过，已推送给专家终审' : '初审拒绝')
    completionReviewVisible.value = false
    refresh()
  } catch (e) {
    console.error(e)
    ElMessage.error('审批失败：' + (e.message || '未知错误'))
  } finally {
    savingCompletionReview.value = false
  }
}

const loadMilestoneReport = async () => {
  if (!milestoneReviewForm.value.milestoneId) {
    currentReport.value = null
    return
  }
  try {
    const res = await getMilestoneReports(milestoneReviewForm.value.milestoneId)
    const list = Array.isArray(res) ? res : (res?.list || [])
    if (list.length > 0) {
      currentReport.value = list[0]
    } else {
      currentReport.value = null
      ElMessage.warning('该里程碑尚未提交报告')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载报告失败')
  }
}

const submitMilestoneReview = async () => {
  if (!milestoneReviewForm.value.milestoneId) {
    ElMessage.warning('请选择里程碑')
    return
  }
  if (milestoneReviewForm.value.approved === null) {
    ElMessage.warning('请选择审批结果')
    return
  }
  savingMilestoneReview.value = true
  try {
    await adminApproveMilestoneReport({
      milestoneId: milestoneReviewForm.value.milestoneId,
      approved: milestoneReviewForm.value.approved,
      feedback: milestoneReviewForm.value.feedback
    })
    ElMessage.success('审批已提交')
    milestoneReviewVisible.value = false
    refresh()
  } catch (e) {
    console.error(e)
    ElMessage.error('审批失败：' + (e.message || '未知错误'))
  } finally {
    savingMilestoneReview.value = false
  }
}

refresh()
</script>

<style scoped>
.incubation-manage {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
</style>


