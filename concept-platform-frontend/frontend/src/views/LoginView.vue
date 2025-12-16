<template>
  <div class="auth-shell">
    <div class="auth-hero surface-card">
      <div class="pill">Concept Platform</div>
      <h1>概灵数智 · 概念孵化平台</h1>
      <p class="muted">将前沿技术创意转化为现实应用，支持项目申报、审核与评审的全流程协作。</p>
      <div class="hero-grid">
        <div class="hero-stat">
          <div class="stat-number">40+</div>
          <div class="muted">在孵项目</div>
        </div>
        <div class="hero-stat">
          <div class="stat-number">12</div>
          <div class="muted">技术领域</div>
        </div>
        <div class="hero-stat">
          <div class="stat-number">24/7</div>
          <div class="muted">在线支撑</div>
        </div>
      </div>
    </div>

    <el-card class="auth-card surface-card" shadow="never">
      <template #header>
        <div class="card-heading">
          <div>
            <div class="title">欢迎回来</div>
            <p class="muted">登录你的项目空间，继续推进申报与评审</p>
          </div>
          <el-button link type="primary" @click="handleRegister">注册</el-button>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="loginFormRef" label-width="70px" class="tech-form" size="large">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-button" :loading="loading" @click="handleLogin" round>登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleRegister = () => {
  router.push('/register')
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(form)
        const userObj = { ...res }
        if (userObj.user_id && !userObj.userId) {
          userObj.userId = userObj.user_id
        }
        if (userObj.id && !userObj.userId) {
          userObj.userId = userObj.id
        }
        localStorage.setItem('user', JSON.stringify(userObj))
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        console.error('登录失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.auth-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(420px, 1.1fr) minmax(420px, 0.9fr);
  gap: 28px;
  align-items: center;
  padding: 60px 7vw 80px;
}

.auth-hero {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  background: linear-gradient(145deg, rgba(47, 106, 230, 0.08), rgba(255, 255, 255, 0.9));
}

.auth-hero h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 800;
}

.hero-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
}

.hero-stat {
  padding: 14px 12px;
  border-radius: var(--radius-md);
  background: var(--bg-muted);
}

.stat-number {
  font-size: 20px;
  font-weight: 800;
  color: var(--primary-700);
}

.auth-card {
  max-width: 520px;
  width: 100%;
  margin-left: auto;
}

.card-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.title {
  font-size: 22px;
  font-weight: 800;
  margin: 0 0 4px;
}

.login-button {
  width: 100%;
  font-size: 15px;
  padding: 12px 0;
}

@media (max-width: 960px) {
  .auth-shell {
    grid-template-columns: 1fr;
    padding: 32px 6vw 48px;
  }

  .auth-card {
    margin: 0;
  }
}
</style>
