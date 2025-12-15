<template>
  <div class="project-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目列表</span>
          <div>
            <el-button type="primary" size="small" @click="$router.push('/project/add')">新增项目</el-button>
            <el-button type="success" size="small" @click="refreshList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" style="width: 100%" stripe>
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
import { ElMessage } from 'element-plus'

const tableData = ref([])
const loading = ref(false)

const getStatusType = (status) => {
  // 0-草稿是灰色(info)，1-待初审是蓝色(primary)
  const map = {
    0: 'info',
    1: 'primary',
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
    const res = await getList({})
    // 兼容处理：如果 res 是数组则直接使用，如果是对象且有 list 属性则使用 list，否则为空数组
    // 注意：request.js 拦截器已经处理了 res.data 的解构 (返回 res.data.data)
    // 假设后端标准返回 data: [] 或 data: { list: [], total: 0 }
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
.project-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

