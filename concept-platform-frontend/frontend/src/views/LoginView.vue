<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>登录</span>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="loginFormRef" label-width="60px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-button" :loading="loading" @click="handleLogin">登录</el-button>
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

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        const res = await login(form)
        
        // 1. 打印后端返回的数据，便于调试
        console.log('Login API Response:', res)

        // 2. 数据处理与映射
        // 确保 user 对象中包含 userId
        const userObj = { ...res }
        
        // 映射 user_id -> userId
        if (userObj.user_id && !userObj.userId) {
          userObj.userId = userObj.user_id
        }
        // 映射 id -> userId (如果后端返回的是 id)
        if (userObj.id && !userObj.userId) {
          userObj.userId = userObj.id
        }

        // 3. 存入 localStorage
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.login-card {
  width: 400px;
}

.card-header {
  text-align: center;
  font-weight: bold;
  font-size: 18px;
}

.login-button {
  width: 100%;
}
</style>

