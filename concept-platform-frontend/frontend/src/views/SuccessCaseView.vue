<template>
  <div class="page-wrap success-case">
    <div class="page-header">
      <div>
        <h2 class="page-title">成功案例库</h2>
        <p class="page-subtitle">展示已完成孵化的项目成果，可投入使用</p>
      </div>
      <el-button type="primary" plain size="small" @click="refresh">刷新</el-button>
    </div>

    <div class="stats-grid">
      <el-card class="surface-card" shadow="never">
        <template #header>
          <div class="section-heading">
            <span>技术领域分布</span>
          </div>
        </template>
        <div ref="domainChartRef" class="chart" />
      </el-card>

      <el-card class="surface-card" shadow="never">
        <template #header>
          <div class="section-heading">
            <span>类别数量柱状</span>
          </div>
        </template>
        <div ref="trendChartRef" class="chart" />
      </el-card>
    </div>

    <el-card class="surface-card" shadow="never">
      <template #header>
        <div class="section-heading">
          <span>成功案例列表</span>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" style="width: 100%" stripe class="tech-table">
        <el-table-column prop="caseName" label="案例名称" min-width="200" />
        <el-table-column prop="techDomain" label="技术领域" width="180">
          <template #default="scope">
            {{ formatTechDomain(scope.row.techDomain) }}
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览次数" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ scope.row.createdAt ? scope.row.createdAt.replace('T', ' ').substring(0, 16) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="viewDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 案例详情对话框 -->
    <el-dialog v-model="detailVisible" title="案例详情" width="800px">
      <el-descriptions v-if="currentCase" :column="1" border>
        <el-descriptions-item label="案例名称">{{ currentCase.caseName }}</el-descriptions-item>
        <el-descriptions-item label="技术领域">
          {{ formatTechDomain(currentCase.techDomain) }}
        </el-descriptions-item>
        <el-descriptions-item label="案例描述">
          <div style="white-space: pre-wrap;">{{ currentCase.caseDescription }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="应用场景">
          <div style="white-space: pre-wrap;">{{ currentCase.applicationScenario }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="知识产权">
          <div style="white-space: pre-wrap;">{{ currentCase.intellectualProperty }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="合作模式">
          {{ formatTechDomain(currentCase.cooperationMode) }}
        </el-descriptions-item>
        <el-descriptions-item label="联系方式">
          {{ currentCase.contactInfo || '未提供' }}
        </el-descriptions-item>
        <el-descriptions-item label="浏览次数">{{ currentCase.viewCount || 0 }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getPublicSuccessCases, getCaseDetail, getCaseDomainStats } from '@/api/successCase'
import { formatTechDomain } from '@/utils/format'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const currentCase = ref(null)

const domainChartRef = ref(null)
const trendChartRef = ref(null)
let domainChartInstance = null
let trendChartInstance = null
const domainStats = ref([])
const trendStats = ref([])

const initCharts = () => {
  if (domainChartRef.value && !domainChartInstance) {
    domainChartInstance = echarts.init(domainChartRef.value)
  }
  if (trendChartRef.value && !trendChartInstance) {
    trendChartInstance = echarts.init(trendChartRef.value)
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
          selectedMode: false,
          avoidLabelOverlap: true,
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
          labelLine: { show: true },
          color: ['#22d3ee', '#38bdf8', '#4ade80', '#a855f7', '#f97316', '#facc15'],
          emphasis: {
            scale: true,
            scaleSize: 10,
            focus: 'self',
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
            selected: false
          }))
        }
      ]
    })
  }

  if (trendChartInstance) {
    const categories = domainStats.value.map(item => item.name)
    const values = domainStats.value.map(item => item.value)

    trendChartInstance.setOption({
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
        data: categories,
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
          name: '案例数量',
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
          data: values,
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

const fetchStats = async () => {
  try {
    const domainRes = await getCaseDomainStats()
    domainStats.value = Array.isArray(domainRes) ? domainRes : []
    trendStats.value = domainStats.value
    await nextTick()
    renderCharts()
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

const refresh = async () => {
  loading.value = true
  try {
    const res = await getPublicSuccessCases()
    tableData.value = Array.isArray(res) ? res : (res?.list || [])
    await fetchStats()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await getCaseDetail(row.caseId)
    currentCase.value = res
    detailVisible.value = true
  } catch (e) {
    console.error(e)
    ElMessage.error('获取案例详情失败')
  }
}

onMounted(() => {
  refresh()
  window.addEventListener('resize', renderCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', renderCharts)
  domainChartInstance?.dispose()
  trendChartInstance?.dispose()
})
</script>

<style scoped>
.success-case {
  display: flex;
  flex-direction: column;
  gap: 14px;
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
</style>

