<template>
  <div class="layout-shell">
    <aside class="nav-rail surface-card">
      <div class="brand">
        <div class="brand-mark">
          <img :src="logoUrl" alt="平台 Logo" class="brand-logo" />
        </div>
        <div class="brand-text">
          <div class="brand-title">概念验证平台</div>
          <p class="brand-sub">Concept Platform</p>
        </div>
      </div>
      <el-menu :default-active="activeMenu" class="nav-menu" router>
        <template v-if="userRole === 'APPLICANT' || userRole === 'student'">
          <el-menu-item index="/my-projects">
            <el-icon><Document /></el-icon>
            <span>我的项目</span>
          </el-menu-item>
          <el-menu-item index="/my-incubation-projects">
            <el-icon><Finished /></el-icon>
            <span>我的孵化项目</span>
          </el-menu-item>
          <el-menu-item index="/project/add">
            <el-icon><Plus /></el-icon>
            <span>新建申报</span>
          </el-menu-item>
        </template>

        <template v-if="userRole === 'ADMIN'">
          <el-menu-item index="/audit-projects">
            <el-icon><Finished /></el-icon>
            <span>项目审核</span>
          </el-menu-item>
          <el-menu-item index="/incubation-manage">
            <el-icon><Document /></el-icon>
            <span>孵化项目管理</span>
          </el-menu-item>
        </template>

        <template v-if="userRole === 'EXPERT'">
          <el-menu-item index="/expert-reviews">
            <el-icon><ChatDotSquare /></el-icon>
            <span>评审任务</span>
          </el-menu-item>
          <el-menu-item index="/mentor-projects">
            <el-icon><User /></el-icon>
            <span>我的辅导项目</span>
          </el-menu-item>
        </template>
        
        <el-menu-item index="/success-cases">
          <el-icon><Trophy /></el-icon>
          <span>成功案例库</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="main-stack">
      <header class="app-header surface-card">
        <div class="header-left">
          <p class="eyebrow">概念平台 · 控制台</p>
          <div class="user-block">
            <div class="user-name">{{ username }}</div>
            <span class="muted">{{ userRole }}</span>
          </div>
        </div>
        <div class="header-actions">
          <el-button type="primary" plain @click="logout">退出登录</el-button>
        </div>
      </header>

      <main class="app-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Document, Plus, Finished, ChatDotSquare, User, Trophy } from '@element-plus/icons-vue'
import logoUrl from '../../renmin-university-of-china-logo-1024px.png'

const router = useRouter()
const route = useRoute()

const user = ref({})
const username = computed(() => user.value.username || 'Unknown')
const userRole = computed(() => user.value.role || 'GUEST')

const activeMenu = computed(() => route.path)

onMounted(() => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    try {
      user.value = JSON.parse(storedUser)
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
.layout-shell {
  display: grid;
  grid-template-columns: 240px 1fr;
  min-height: 100vh;
  gap: 18px;
  padding: 18px;
}

.nav-rail {
  padding: 18px 14px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  background: var(--bg-muted);
}

.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 20%, #ffffff, rgba(255, 255, 255, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.18);
}

.brand-logo {
  width: 124%;
  height: 124%;
  object-fit: contain;
  transform: translateY(3%);
  filter: drop-shadow(0 0 8px rgba(148, 27, 27, 0.4));
}

.brand-title {
  font-weight: 700;
}

.brand-sub {
  margin: 0;
  color: var(--text-muted);
  font-size: 12px;
}

.nav-menu {
  flex: 1;
  background: transparent;
}

.main-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.app-header {
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.eyebrow {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
  letter-spacing: 0.2px;
}

.user-block {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
}

.user-name {
  font-size: 18px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-main {
  padding: 0 6px 10px 6px;
}

@media (max-width: 1100px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }

  .nav-rail {
    flex-direction: row;
    align-items: center;
  }

  .nav-menu {
    width: 100%;
  }
}
</style>
