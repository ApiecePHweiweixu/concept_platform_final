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
            <el-table-column prop="techDomain" label="技术领域" width="150" />
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
            <el-table-column prop="techDomain" label="技术领域" width="150" />
            <el-table-column prop="status" label="当前状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusLabel(scope.row.status) }}
                </el-tag>
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
      <el-form :model="auditForm" label-width="80px" class="tech-form">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.action">
            <el-radio value="PASS">通过并指派</el-radio>
            <el-radio value="REJECT">驳回</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="auditForm.action === 'PASS'">
          <el-form-item label="指派专家" required>
            <el-checkbox-group v-model="auditForm.expertIds">
              <el-checkbox v-for="item in expertList" :key="item.userId" :label="item.userId">
                {{ item.realName || item.username }} ({{ item.field || '通用' }})
              </el-checkbox>
            </el-checkbox-group>
            <div class="muted small">请选择 1-3 名专家</div>
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
        <el-descriptions-item label="技术领域">{{ currentDetail.techDomain }}</el-descriptions-item>
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
import { getList, auditProject } from '@/api/project'
import { getExperts } from '@/api/user'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const submitting = ref(false)
const expertList = ref([])
const activeTab = ref('pending')
const allProjects = ref([])
const currentDetail = ref({})

const auditForm = reactive({
  projectId: null,
  action: 'PASS',
  expertIds: [],
  comment: ''
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

const handleAudit = (row) => {
  auditForm.projectId = row.id || row.projectId
  auditForm.action = 'PASS'
  auditForm.expertIds = []
  auditForm.comment = ''
  dialogVisible.value = true
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
</style>
