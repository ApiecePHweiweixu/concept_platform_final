<template>
  <div class="page-wrap my-projects">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的项目</h2>
        <p class="page-subtitle">跟踪申报进度、查看统计与评审反馈</p>
      </div>
      <el-button type="primary" plain size="small" @click="refreshAll">刷新数据</el-button>
    </div>

    <div class="hero surface-card">
      <div class="hero-left">
        <p class="pill">概念验证平台 · 实时概览</p>
        <h1>概念平台 — 项目申报与评审</h1>
        <p class="muted">将创新想法沉淀为可落地的应用，支持项目申报、专家评审与全流程跟踪。</p>
        <div class="hero-chips">
          <span class="chip">流程透明</span>
          <span class="chip">专家评审</span>
          <span class="chip">数据统计</span>
        </div>
      </div>
      <div class="hero-right">
        <div class="blob blob-1"></div>
        <div class="blob blob-2"></div>
        <div class="blob blob-3"></div>
      </div>
    </div>

    <div class="stats-grid">
      <el-card class="surface-card" shadow="never">
        <template #header>
          <div class="section-heading">
            <span>技术领域分布</span>
            <small class="muted">实时拉取数据库统计</small>
          </div>
        </template>
        <div ref="domainChartRef" class="chart" />
      </el-card>

      <el-card class="surface-card" shadow="never">
        <template #header>
          <div class="section-heading">
            <span>项目状态分布</span>
            <small class="muted">待审核 / 评审中 / 已入库</small>
          </div>
        </template>
        <div ref="statusChartRef" class="chart" />
      </el-card>
    </div>

    <el-card class="surface-card" shadow="never">
      <template #header>
        <div class="section-heading">
          <span>我的项目</span>
          <el-button type="primary" size="small" @click="refreshAll">刷新数据</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" style="width: 100%" stripe class="tech-table">
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

    <el-dialog v-model="feedbackVisible" title="评审反馈" width="600px">
      <div v-if="currentProject.status === 9">
        <el-alert title="项目已被驳回" type="error" :closable="false" show-icon />
        <div class="reject-box">
          <p><strong>驳回理由：</strong></p>
          <div class="reject-reason">
            {{ currentProject.rejectReason || '无详细理由' }}
          </div>
        </div>
      </div>

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
          <el-table-column prop="comments" label="评语" />
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
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getMyProjectList, getDomainStats, getStatusStats } from '@/api/project'
import { getProjectReviews } from '@/api/review'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const loading = ref(false)
const feedbackVisible = ref(false)
const currentProject = ref({})
const reviewList = ref([])
const domainChartRef = ref(null)
const statusChartRef = ref(null)
let domainChartInstance = null
let statusChartInstance = null
const domainStats = ref([])
const statusStats = ref([])

const averageScore = computed(() => {
  if (!reviewList.value || reviewList.value.length === 0) return 0
  const sum = reviewList.value.reduce((acc, curr) => acc + (curr.score || 0), 0)
  return sum / reviewList.value.length
})

const initCharts = () => {
  if (domainChartRef.value && !domainChartInstance) {
    domainChartInstance = echarts.init(domainChartRef.value)
  }
  if (statusChartRef.value && !statusChartInstance) {
    statusChartInstance = echarts.init(statusChartRef.value)
  }
}

const renderCharts = () => {
  initCharts()

  if (domainChartInstance) {
    domainChartInstance.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item' },
      legend: {
        top: 10,
        textStyle: { color: '#4b5563' }
      },
      series: [
        {
          name: '技术领域',
          type: 'pie',
          radius: ['35%', '65%'],
          roseType: 'area',
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.1)'
          },
          label: { color: '#1f2937' },
          data: domainStats.value
        }
      ]
    })
  }

  if (statusChartInstance) {
    const order = ['待审核', '评审中', '已入库', '已驳回']
    const valueMap = statusStats.value.reduce((acc, cur) => {
      acc[cur.name] = cur.value
      return acc
    }, {})
    const barData = order.map(name => valueMap[name] || 0)

    statusChartInstance.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 30, bottom: 30 },
      xAxis: {
        type: 'category',
        data: order,
        axisLine: { lineStyle: { color: '#a5b4fc' } },
        axisLabel: { color: '#475569' }
      },
      yAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: '#a5b4fc' } },
        splitLine: { lineStyle: { color: 'rgba(148,163,184,0.25)' } },
        axisLabel: { color: '#475569' }
      },
      series: [
        {
          name: '数量',
          type: 'bar',
          barWidth: '45%',
          itemStyle: {
            borderRadius: [6, 6, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2f6ae6' },
              { offset: 1, color: '#61a0ff' }
            ])
          },
          data: barData
        }
      ]
    })
  }
}

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

const fetchStats = async () => {
  try {
    const [domainRes, statusRes] = await Promise.all([
      getDomainStats(),
      getStatusStats()
    ])
    domainStats.value = Array.isArray(domainRes) ? domainRes : (domainRes?.list || [])
    statusStats.value = Array.isArray(statusRes) ? statusRes : (statusRes?.list || [])
    await nextTick()
    renderCharts()
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
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

const refreshAll = () => {
  fetchData()
  fetchStats()
}

const handleViewFeedback = async (row) => {
  currentProject.value = row
  feedbackVisible.value = true
  reviewList.value = []

  if (row.status === 3) {
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
  fetchStats()
  window.addEventListener('resize', renderCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', renderCharts)
  domainChartInstance?.dispose()
  statusChartInstance?.dispose()
})
</script>

<style scoped>
.my-projects {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: calc(100vh - 120px);
}

.hero {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-lg);
  padding: 32px;
  display: grid;
  grid-template-columns: minmax(320px, 2fr) minmax(200px, 1fr);
  gap: 16px;
  background: linear-gradient(120deg, rgba(47, 106, 230, 0.08), rgba(255, 255, 255, 0.95));
}

.hero-left h1 {
  margin: 10px 0 12px;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: 0.5px;
  color: var(--text-primary);
}

.hero-left .muted {
  margin: 0 0 14px;
}

.hero-chips {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  background: var(--bg-muted);
  border-radius: 12px;
  color: var(--text-primary);
  font-weight: 600;
  font-size: 13px;
}

.hero-right {
  position: relative;
  min-height: 160px;
}

.blob {
  position: absolute;
  filter: blur(24px);
  opacity: 0.6;
}

.blob-1 {
  width: 160px;
  height: 160px;
  background: #c7d2fe;
  top: 10px;
  right: 40px;
  border-radius: 50%;
}

.blob-2 {
  width: 140px;
  height: 140px;
  background: #bae6fd;
  top: 40px;
  left: 10px;
  border-radius: 50%;
}

.blob-3 {
  width: 120px;
  height: 120px;
  background: #bfdbfe;
  bottom: 0;
  right: 0;
  border-radius: 50%;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.chart {
  width: 100%;
  height: 320px;
}

.reject-box {
  margin-top: 20px;
}

.reject-reason {
  background: #fef2f2;
  padding: 10px;
  border-radius: 8px;
  color: #b91c1c;
}

@media (max-width: 960px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
