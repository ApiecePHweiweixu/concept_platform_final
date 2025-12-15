<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px">
        <div class="logo">概念验证平台</div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-demo"
          router
        >
          <!-- APPLICANT (申报人) 菜单 -->
          <template v-if="userRole === 'APPLICANT' || userRole === 'student'">
             <el-menu-item index="/my-projects">
              <el-icon><document /></el-icon>
              <span>我的项目</span>
            </el-menu-item>
             <el-menu-item index="/project/add">
              <el-icon><plus /></el-icon>
              <span>新建申报</span>
            </el-menu-item>
          </template>

          <!-- ADMIN (管理员) 菜单 -->
          <template v-if="userRole === 'ADMIN'">
             <el-menu-item index="/audit-projects">
              <el-icon><finished /></el-icon>
              <span>项目审核</span>
            </el-menu-item>
          </template>

          <!-- EXPERT (专家) 菜单 -->
          <template v-if="userRole === 'EXPERT'">
             <el-menu-item index="/expert-reviews">
              <el-icon><chat-dot-square /></el-icon>
              <span>评审任务</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-content">
            <span>当前用户: {{ username }} ({{ userRole }})</span>
            <el-button link @click="logout">退出登录</el-button>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Document, Plus, Finished, ChatDotSquare } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const user = ref({})
const username = computed(() => user.value.username || 'Unknown')
const userRole = computed(() => user.value.role || 'GUEST') // 默认 GUEST

const activeMenu = computed(() => route.path)

onMounted(() => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    try {
      user.value = JSON.parse(storedUser)
      // 兼容之前的模拟登录逻辑，如果没有 role 默认给一个，或者确保登录时写入 role
      if (!user.value.role && user.value.username === 'student') {
        user.value.role = 'APPLICANT'
      }
    } catch (e) {
      console.error('Failed to parse user info', e)
    }
  }
})

const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.common-layout {
  height: 100vh;
}
.el-container {
  height: 100%;
}
.el-aside {
  background-color: #fff;
  border-right: 1px solid #dcdfe6;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #dcdfe6;
}
.el-header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.header-content {
  display: flex;
  gap: 15px;
  align-items: center;
}
.el-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>

