<template>
  <div class="page-wrap my-incubation">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的孵化项目</h2>
        <p class="page-subtitle">查看入孵进展、提交协议与里程碑报告、申请资源支持</p>
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
        <el-table-column prop="completionStatus" label="落成状态" width="120">
          <template #default="scope">
            <span>{{ getCompletionLabel(scope.row.completionStatus) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              type="primary"
              link
              size="small"
              @click="openAgreementDialog(scope.row)"
            >
              提交入孵协议
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="primary"
              link
              size="small"
              @click="openMilestoneDrawer(scope.row)"
            >
              里程碑与汇报
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="primary"
              link
              size="small"
              @click="openResourceDialog(scope.row)"
            >
              申请资源
            </el-button>
            <el-button
              v-if="scope.row.status === 1 && (scope.row.completionStatus === 0 || scope.row.completionStatus === 4 || scope.row.completionStatus === 5)"
              type="danger"
              link
              size="small"
              @click="openCompletionDialog(scope.row)"
            >
              提交落成申请
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 入孵协议 -->
    <el-dialog v-model="agreementVisible" title="提交入孵协议与信息表" width="620px">
      <el-form :model="agreementForm" label-width="100px" class="tech-form">
        <el-form-item label="协议附件URL">
          <el-input v-model="agreementForm.agreementUrl" placeholder="例如 /uploads/incubation-agreement.pdf" />
        </el-form-item>
        <el-form-item label="入孵信息表(JSON)">
          <el-input
            v-model="agreementForm.infoFormJson"
            type="textarea"
            rows="4"
            placeholder='可先简单填写，如 {"teamSize":3,"need":"办公场地"}'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="agreementVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingAgreement" @click="submitAgreementForm">提交</el-button>
      </template>
    </el-dialog>

    <!-- 里程碑侧边栏 -->
    <el-drawer v-model="milestoneDrawerVisible" title="里程碑与进展" size="520px">
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
              v-if="m.status === 1 || m.status === 4"
              type="primary"
              link
              size="small"
              @click="openReportDialog(m)"
            >
              提交/修改报告
            </el-button>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>

    <!-- 进展报告 -->
    <el-dialog v-model="reportVisible" title="提交里程碑进展报告" width="620px">
      <el-form :model="reportForm" label-width="100px" class="tech-form">
        <el-form-item label="里程碑">
          <el-input v-model="reportForm.milestoneName" disabled />
        </el-form-item>
        <el-form-item label="进展报告">
          <el-input
            v-model="reportForm.content"
            type="textarea"
            rows="5"
            placeholder="说明完成情况、遇到的问题、下一步计划"
          />
        </el-form-item>
        <el-form-item label="附件(JSON)" required>
          <el-input
            v-model="reportForm.attachments"
            type="textarea"
            rows="3"
            placeholder='例如 [{"name":"演示视频","url":"/uploads/demo.mp4"}]'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingReport" @click="submitReport">提交</el-button>
      </template>
    </el-dialog>

    <!-- 资源申请 -->
    <el-dialog v-model="resourceVisible" title="资源申请" width="600px">
      <el-form :model="resourceForm" label-width="100px" class="tech-form">
        <el-form-item label="类型">
          <el-select v-model="resourceForm.type" placeholder="请选择">
            <el-option label="技术咨询" value="TECH" />
            <el-option label="知识产权辅导" value="IP" />
            <el-option label="市场渠道对接" value="MARKET" />
            <el-option label="融资路演机会" value="FUND" />
            <el-option label="拨款支持" value="FUNDING" />
            <el-option label="服务器/算力资源" value="SERVER" />
            <el-option label="技术支持" value="SUPPORT" />
            <el-option label="企业实习" value="INTERNSHIP" />
            <el-option label="辅导/陪跑" value="MENTOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="resourceForm.title" />
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="resourceForm.description" type="textarea" rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resourceVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingResource" @click="submitResource">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 落成申请 -->
    <el-dialog v-model="completionVisible" title="落成申请" width="620px">
      <el-form :model="completionForm" label-width="100px" class="tech-form">
        <el-form-item label="材料包URL">
          <el-input v-model="completionForm.completionPackageUrl" placeholder="最终材料包附件URL" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input
            v-model="completionForm.completionDesc"
            type="textarea"
            rows="4"
            placeholder="简要说明最终成果、知识产权、合作情况等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completionVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingCompletion" @click="submitCompletion">提交落成申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getMyIncubationProjects,
  submitAgreement,
  getMilestones,
  submitMilestoneReport,
  applyResource,
  applyCompletion
} from '@/api/incubation'

const loading = ref(false)
const tableData = ref([])

const agreementVisible = ref(false)
const agreementForm = ref({
  incubationId: null,
  agreementUrl: '',
  infoFormJson: ''
})
const savingAgreement = ref(false)

const milestoneDrawerVisible = ref(false)
const currentIncubation = ref(null)
const milestones = ref([])

const reportVisible = ref(false)
const reportForm = ref({
  milestoneId: null,
  milestoneName: '',
  content: '',
  attachments: ''
})
const savingReport = ref(false)

const resourceVisible = ref(false)
const resourceForm = ref({
  incubationId: null,
  projectId: null,
  type: '',
  title: '',
  description: ''
})
const savingResource = ref(false)

const completionVisible = ref(false)
const completionForm = ref({
  incubationId: null,
  completionPackageUrl: '',
  completionDesc: ''
})
const savingCompletion = ref(false)

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

const getCompletionLabel = (v) => {
  const map = {
    0: '未申请',
    1: '待平台初审',
    2: '待专家终审',
    3: '已落成',
    4: '平台初审拒绝',
    5: '专家终审拒绝'
  }
  return map[v] || '未申请'
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
    const applicantId = user.userId || user.id || user.user_id
    const res = await getMyIncubationProjects({ applicantId })
    // 后端目前只返回 incubation_project，需要前端补一列 projectName（可后续扩展）
    tableData.value = Array.isArray(res) ? res : (res?.list || [])
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openAgreementDialog = (row) => {
  agreementForm.value = {
    incubationId: row.incubationId,
    agreementUrl: row.agreementUrl || '',
    infoFormJson: row.infoFormJson || ''
  }
  agreementVisible.value = true
}

const submitAgreementForm = async () => {
  if (!agreementForm.value.agreementUrl) {
    ElMessage.warning('请填写协议附件URL')
    return
  }
  savingAgreement.value = true
  try {
    await submitAgreement(agreementForm.value)
    ElMessage.success('提交成功')
    agreementVisible.value = false
    refresh()
  } catch (e) {
    console.error(e)
  } finally {
    savingAgreement.value = false
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

const openReportDialog = async (m) => {
  // 打开报告对话框时，优先加载最近一次提交的报告，方便在中期/最终被退回后进行修改
  reportForm.value = {
    milestoneId: m.milestoneId,
    milestoneName: m.name,
    content: '',
    attachments: ''
  }
  reportVisible.value = true

  try {
    // 查询该里程碑下已有报告（按创建时间倒序）
    const res = await getMilestones(currentIncubation.value.incubationId)
    // 这里仅刷新里程碑本身，具体报告内容通过后端里程碑报告接口获取更合适，
    // 如后续有单独的 getMilestoneReports，可在此替换为按 milestoneId 查询报告并预填。
  } catch (e) {
    console.error(e)
  }
}

const submitReport = async () => {
  if (!reportForm.value.content) {
    ElMessage.warning('请填写进展报告')
    return
  }
  if (!reportForm.value.attachments) {
    ElMessage.warning('请填写附件信息（至少上传一个附件）')
    return
  }
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.error('未登录')
    return
  }
  const user = JSON.parse(userStr)
  const submitterId = user.userId || user.id || user.user_id
  savingReport.value = true
  try {
    await submitMilestoneReport({
      milestoneId: reportForm.value.milestoneId,
      milestoneName: reportForm.value.milestoneName,
      projectId: currentIncubation.value.projectId,
      submitterId,
      // 新增项目描述字段，当前简单复用进展内容，后续可单独拆分输入项
      projectDescription: reportForm.value.content,
      content: reportForm.value.content,
      attachments: reportForm.value.attachments
    })
    ElMessage.success('报告已提交，等待管理员审批')
    reportVisible.value = false
    // 重新加载里程碑状态
    if (currentIncubation.value) {
      const res = await getMilestones(currentIncubation.value.incubationId)
      milestones.value = Array.isArray(res) ? res : (res?.list || [])
    }
  } catch (e) {
    console.error(e)
  } finally {
    savingReport.value = false
  }
}

const openResourceDialog = (row) => {
  resourceForm.value = {
    incubationId: row.incubationId,
    projectId: row.projectId,
    type: '',
    title: '',
    description: ''
  }
  resourceVisible.value = true
}

const submitResource = async () => {
  if (!resourceForm.value.type || !resourceForm.value.title) {
    ElMessage.warning('请填写类型和标题')
    return
  }
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.error('未登录')
    return
  }
  const user = JSON.parse(userStr)
  const requesterId = user.userId || user.id || user.user_id
  savingResource.value = true
  try {
    await applyResource({
      ...resourceForm.value,
      requesterId
    })
    ElMessage.success('资源申请已提交')
    resourceVisible.value = false
  } catch (e) {
    console.error(e)
  } finally {
    savingResource.value = false
  }
}

const openCompletionDialog = (row) => {
  completionForm.value = {
    incubationId: row.incubationId,
    completionPackageUrl: '',
    completionDesc: ''
  }
  completionVisible.value = true
}

const submitCompletion = async () => {
  if (!completionForm.value.completionPackageUrl) {
    ElMessage.warning('请填写材料包URL')
    return
  }
  savingCompletion.value = true
  try {
    await applyCompletion(completionForm.value)
    ElMessage.success('落成申请已提交，等待平台审核')
    completionVisible.value = false
    refresh()
  } catch (e) {
    console.error(e)
    // 显式提示后端返回的错误信息（例如：需要至少完成一轮中期里程碑审核）
    if (e && e.message) {
      ElMessage.error(e.message)
    } else {
      ElMessage.error('提交失败，请确认至少已有一轮里程碑（中期汇报）通过审核后再尝试')
    }
  } finally {
    savingCompletion.value = false
  }
}

refresh()
</script>

<style scoped>
.my-incubation {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.small {
  font-size: 12px;
}
</style>


