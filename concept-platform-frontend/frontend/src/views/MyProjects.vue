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
            <small class="muted"> 实时拉取数据库统计</small>
          </div>
        </template>
        <div ref="domainChartRef" class="chart" />
      </el-card>

      <el-card class="surface-card" shadow="never">
        <template #header>
          <div class="section-heading">
            <span>项目状态分布</span>
            <small class="muted"> 草稿 / 待审核 / 评审中 / 已入库 / 已驳回</small>
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
        <el-table-column prop="techDomain" label="技术领域" width="150">
          <template #default="scope">
            {{ formatTechDomain(scope.row.techDomain) }}
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 0"
              type="primary" 
              link
              size="small" 
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button 
              type="primary" 
              link
              size="small" 
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
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
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getMyProjectList, getDomainStats, getStatusStats } from '@/api/project'
import { getProjectReviews } from '@/api/review'
import { formatTechDomain } from '@/utils/format'
import { ElMessage } from 'element-plus'

const router = useRouter()

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
      backgroundColor: '#020617',
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(15,23,42,0.92)',
        borderColor: 'rgba(45,212,191,0.8)',
        borderWidth: 1,
        textStyle: { color: '#e0f2fe' }
      },
      legend: {
        top: 12,
        icon: 'circle',
        itemWidth: 10,
        itemHeight: 10,
        textStyle: { color: '#9ca3af' }
      },
      series: [
        {
          name: '技术领域',
          type: 'pie',
          radius: ['32%', '62%'],
          // 移除 roseType，使用普通饼图，所有扇形角度相等
          selectedMode: false, // 禁用选中状态
          avoidLabelOverlap: true, // 防止标签重叠
          itemStyle: {
            borderColor: 'rgba(34, 211, 238, 0.8)',
            borderWidth: 1.4,
            shadowBlur: 25,
            shadowColor: 'rgba(56,189,248,0.7)'
          },
          label: {
            color: '#e5f9ff',
            fontSize: 12,
            textShadowBlur: 6,
            textShadowColor: 'rgba(15,23,42,0.9)',
            formatter: '{b}: {c}',
            show: true
          },
          labelLine: {
            show: true
          },
          color: [
            '#22d3ee',
            '#38bdf8',
            '#4ade80',
            '#a855f7',
            '#f97316',
            '#facc15'
          ],
          emphasis: {
            scale: true, // 启用鼠标悬停时的缩放效果
            scaleSize: 10, // 缩放大小
            focus: 'self', // 聚焦到当前扇形
            itemStyle: {
              shadowBlur: 30,
              shadowColor: 'rgba(34,211,238,0.8)',
              borderWidth: 2,
              borderColor: 'rgba(56,189,248,1)'
            },
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          data: domainStats.value.map(item => ({
            name: item.name,
            value: item.value,
            selected: false // 确保没有默认选中
          }))
        }
      ]
    })
  }

  if (statusChartInstance) {
    const order = ['草稿', '待审核', '评审中', '已入库', '已驳回']
    const valueMap = statusStats.value.reduce((acc, cur) => {
      acc[cur.name] = cur.value
      return acc
    }, {})
    const barData = order.map(name => valueMap[name] || 0)

    statusChartInstance.setOption({
      backgroundColor: '#020617',
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(15,23,42,0.92)',
        borderColor: 'rgba(56,189,248,0.8)',
        borderWidth: 1,
        textStyle: { color: '#e0f2fe' },
        axisPointer: {
          type: 'shadow',
          shadowStyle: {
            color: 'rgba(8,47,73,0.9)'
          }
        }
      },
      grid: { left: 40, right: 20, top: 40, bottom: 32 },
      xAxis: {
        type: 'category',
        data: order,
        axisLine: { lineStyle: { color: '#38bdf8' } },
        axisLabel: { color: '#9ca3af' },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: '#0f172a' } },
        splitLine: {
          lineStyle: {
            color: ['rgba(15,118,110,0.6)'],
            type: 'dashed'
          }
        },
        axisLabel: { color: '#6b7280' }
      },
      series: [
        {
          name: '数量',
          type: 'bar',
          barWidth: '46%',
          itemStyle: {
            borderRadius: [9, 9, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#4ade80' },
              { offset: 0.4, color: '#22d3ee' },
              { offset: 1, color: '#0f172a' }
            ]),
            borderColor: 'rgba(45,212,191,0.9)',
            borderWidth: 1.4,
            shadowBlur: 24,
            shadowColor: 'rgba(34,211,238,0.9)'
          },
          data: barData,
          emphasis: {
            itemStyle: {
              shadowBlur: 42,
              shadowColor: 'rgba(56,189,248,1)',
              borderColor: '#e0f2fe'
            }
          }
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
    
    console.log('后端返回的domainRes:', JSON.stringify(domainRes, null, 2))
    
    // 后端返回的数据应该是一个数组，包含 {name, value} 对象
    // request.js 的拦截器已经提取了 Result.data，所以 domainRes 直接就是 List<StatItem>
    const rawDomainStats = Array.isArray(domainRes) ? domainRes : []
    
    console.log('原始领域统计数据:', rawDomainStats)
    
    // 定义6个标准技术领域，按固定顺序（与提交表单的6个选项一致）
    const standardDomains = [
      '人工智能',
      '大数据与云计算',
      '信息安全',
      '物联网与嵌入式技术',
      '数字人文技术',
      '其他'
    ]
    
    // 创建领域到数量的映射
    const domainMap = new Map()
    rawDomainStats.forEach((item) => {
      // 后端返回的字段应该是小写的 name 和 value
      const domainName = (item.name || '').trim()
      const domainValue = Number(item.value) || 0
      
      console.log(`处理: name="${domainName}", value=${domainValue}`)
      
      // 只处理标准领域，忽略不在标准列表中的领域
      if (domainName && standardDomains.includes(domainName) && domainValue > 0) {
        domainMap.set(domainName, domainValue)
      }
    })
    
    console.log('领域映射结果:', Array.from(domainMap.entries()))
    
    // 按固定顺序构建统计数据，只显示有数据的标准领域
    domainStats.value = standardDomains
      .filter(domain => domainMap.has(domain))
      .map(domain => ({
        name: domain,
        value: domainMap.get(domain)
      }))
    
    console.log('最终domainStats.value:', domainStats.value)
    
    statusStats.value = Array.isArray(statusRes) ? statusRes : []
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

const handleEdit = (row) => {
  // 跳转到编辑页面，传递项目ID
  router.push({
    path: '/project/form',
    query: { id: row.id || row.projectId }
  })
}

const handleView = (row) => {
  // 跳转到查看页面，传递项目ID
  router.push({
    path: '/project/form',
    query: { id: row.id || row.projectId, view: 'true' }
  })
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
  border-radius: 18px;
  overflow: hidden;
  background:
    radial-gradient(circle at 10% 0%, rgba(34, 197, 235, 0.16), transparent 60%),
    radial-gradient(circle at 90% 100%, rgba(52, 211, 153, 0.18), transparent 60%),
    linear-gradient(135deg, #020617, #020617 40%, #020617 60%, #020617);
  box-shadow:
    0 0 0 1px rgba(15, 23, 42, 0.9),
    0 0 18px rgba(34, 211, 238, 0.4),
    0 30px 60px rgba(15, 23, 42, 0.85);
  position: relative;
}

.chart::before {
  content: '';
  position: absolute;
  inset: 10px 14px 14px 10px;
  border-radius: 16px;
  border: 1px solid rgba(75, 85, 99, 0.8);
  border-image: linear-gradient(
      120deg,
      rgba(56, 189, 248, 0.6),
      rgba(107, 114, 128, 0.3),
      rgba(45, 212, 191, 0.7)
    )
    1;
  pointer-events: none;
  mix-blend-mode: screen;
}

.chart::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle at 50% 50%, rgba(148, 163, 184, 0.16) 0, transparent 55%);
  mix-blend-mode: soft-light;
  pointer-events: none;
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

.stats-grid .section-heading {
  justify-content: flex-start;
  gap: 12px;
}

.stats-grid .section-heading .muted {
  margin-left: 0;
}

@media (max-width: 960px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
