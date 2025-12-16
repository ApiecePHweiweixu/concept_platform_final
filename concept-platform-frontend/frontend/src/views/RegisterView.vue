<template>
  <div class="auth-shell single">
    <el-card class="auth-card surface-card" shadow="never">
      <template #header>
        <div class="card-heading">
          <div>
            <div class="title">创建账户</div>
            <p class="muted">注册后即可提交项目并跟进审核</p>
          </div>
          <el-button link type="primary" @click="$router.push('/login')">返回登录</el-button>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="registerFormRef" label-width="90px" status-icon class="tech-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="所属单位" prop="company">
          <el-input v-model="form.company" placeholder="请输入所属单位 (选填)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit" :loading="loading" @click="handleRegister">立即注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  company: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const handleRegister = () => {
  if (!registerFormRef.value) return

  registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const submitData = {
          username: form.username,
          password: form.password,
          realName: form.realName,
          company: form.company
        }

        await register(submitData)
        ElMessage.success('注册成功')
        router.push('/login')
      } catch (error) {
        console.error('注册失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.auth-shell.single {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 60px 20px;
}

.auth-card {
  width: 520px;
}

.card-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.title {
  font-size: 22px;
  font-weight: 800;
  margin: 0 0 4px;
}

.submit {
  width: 100%;
  padding: 12px 0;
}

@media (max-width: 640px) {
  .auth-card {
    width: 100%;
  }
}
</style>
