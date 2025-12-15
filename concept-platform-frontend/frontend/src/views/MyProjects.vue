<template>
  <div class="my-projects-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的项目</span>
          <el-button type="primary" size="small" @click="fetchData">刷新</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="projectName" label="项目名称" min-width="150" />
        <el-table-column prop="techDomain" label="技术领域" width="150" />
        <el-table-column prop="budget" label="预期经费 (万元)" width="150" />
        <el-table-column prop="status" label="当前状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ scope.row.createdAt ? scope.row.createdAt.replace('T', ' ') : '' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 3 || scope.row.status === 9"
              type="primary" 
              link
              size="small" 
              @click="handleViewFeedback(scope.row)"
            >
              查看反馈
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 反馈弹窗 -->
    <el-dialog v-model="feedbackVisible" title="评审反馈" width="600px">
      <!-- 情况A: 初审被拒 -->
      <div v-if="currentProject.status === 9">
        <el-alert title="项目已被驳回" type="error" :closable="false" show-icon />
        <div style="margin-top: 20px;">
          <p><strong>驳回理由：</strong></p>
          <div style="background: #fef0f0; padding: 10px; border-radius: 4px; color: #f56c6c;">
            {{ currentProject.rejectReason || '无详细理由' }}
          </div>
        </div>
      </div>

      <!-- 情况B: 专家评审 -->
      <div v-else-if="currentProject.status === 3">
        <div style="margin-bottom: 20px;">
          <el-statistic title="平均评分" :value="averageScore" precision="1" />
        </div>
        
        <el-table :data="reviewList" border style="width: 100%">
          <el-table-column prop="expertName" label="专家" width="120">
             <template #default="scope">
               专家 {{ scope.$index + 1 }}
             </template>
          </el-table-column>
          <el-table-column prop="score" label="评分" width="80" />
          <el-table-column prop="comment" label="评语" />
        </el-table>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="feedbackVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMyProjectList } from '@/api/project'
import { getProjectReviews } from '@/api/review'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const loading = ref(false)
const feedbackVisible = ref(false)
const currentProject = ref({})
const reviewList = ref([])

const averageScore = computed(() => {
  if (!reviewList.value || reviewList.value.length === 0) return 0
  const sum = reviewList.value.reduce((acc, curr) => acc + (curr.score || 0), 0)
  return sum / reviewList.value.length
})

const getStatusType = (status) => {
  const map = {
    0: 'info',    // 草稿
    1: 'primary', // 待初审
    2: 'warning', // 评审中
    3: 'success', // 已入库
    9: 'danger'   // 已驳回
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
    const userStr = localStorage.getItem('user')
    let userId = null
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        userId = user.userId || user.id || user.user_id
      } catch (e) {
        console.error('JSON Parse Error', e)
      }
    }

    if (!userId) {
      ElMessage.warning('未能获取当前用户ID，无法加载数据')
      return
    }

    const res = await getMyProjectList({ applicantId: userId })
    
    if (Array.isArray(res)) {
      tableData.value = res
    } else if (res && Array.isArray(res.list)) {
      tableData.value = res.list
    } else {
      tableData.value = []
    }
  } catch (error) {
    console.error('获取我的项目列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleViewFeedback = async (row) => {
  currentProject.value = row
  feedbackVisible.value = true
  reviewList.value = [] // 重置列表

  if (row.status === 3) {
    // 只有状态为3（已入库/评审结束）才去拉取评审详情
    // 状态9（驳回）直接显示 row 中的 rejectReason
    try {
      const res = await getProjectReviews(row.id || row.projectId)
      if (Array.isArray(res)) {
        reviewList.value = res
      } else if (res && Array.isArray(res.list)) {
        reviewList.value = res.list
      }
    } catch (error) {
      console.error('获取评审详情失败', error)
      ElMessage.error('获取评审详情失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.my-projects-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
