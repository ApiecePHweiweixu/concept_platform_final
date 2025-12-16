<template>
  <div class="page-wrap project-list">
    <div class="page-header">
      <div>
        <h2 class="page-title">项目列表</h2>
        <p class="page-subtitle">查看所有项目并快速创建/刷新</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" size="small" @click="$router.push('/project/add')">新增项目</el-button>
        <el-button type="primary" plain size="small" @click="refreshList">刷新</el-button>
      </div>
    </div>

    <el-card class="surface-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" style="width: 100%" stripe class="tech-table">
        <el-table-column prop="projectName" label="项目名称" min-width="150" />
        <el-table-column prop="techDomain" label="技术领域" width="150" />
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
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getList } from '@/api/project'

const tableData = ref([])
const loading = ref(false)

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
      tableData.value = res
    } else if (res && Array.isArray(res.list)) {
      tableData.value = res.list
    } else {
      tableData.value = []
    }
  } catch (error) {
    console.error('获取项目列表失败', error)
  } finally {
    loading.value = false
  }
}

const refreshList = () => {
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.project-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.header-actions {
  display: flex;
  gap: 8px;
}
</style>
