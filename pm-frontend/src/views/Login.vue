<template>
  <div class="login-wrapper">
    <div class="login-outer">
      <div class="login-inner">
        <h1>项目管理系统</h1>
        <p class="subtitle">四川省林业和草原规划院</p>
        <el-form :model="form" :rules="rules" ref="formRef" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password
              @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登 录</el-button>
          </el-form-item>
        </el-form>
        <div class="test-hint">测试账号：manager1 / engineer1 / leader1 / admin，密码均为 123456</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const formRef = ref(null)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    if (auth.user?.role === 'leader') {
      router.push('/leader-dashboard')
    } else {
      router.push('/')
    }
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(ellipse at 50% 20%, rgba(26,39,68,0.04) 0%, transparent 60%),
    linear-gradient(180deg, #F5F3EF 0%, #EDEAE4 100%);
}

/* Double-bezel card */
.login-outer {
  padding: 4px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(0,0,0,0.03), rgba(0,0,0,0.06));
  box-shadow:
    0 2px 4px rgba(0,0,0,0.02),
    0 8px 24px rgba(0,0,0,0.06);
}

.login-inner {
  width: 400px;
  padding: 44px 40px 36px;
  background: var(--pm-surface);
  border-radius: 14px;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.8);
}

.login-inner h1 {
  text-align: center;
  font-size: 32px;
  font-weight: 600;
  color: var(--pm-text);
  letter-spacing: -0.02em;
  margin-bottom: 6px;
}

.subtitle {
  text-align: center;
  color: var(--pm-text-muted);
  margin-bottom: 36px;
  font-size: 16px;
  letter-spacing: 0.03em;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0.04em;
}

.test-hint {
  text-align: center;
  color: var(--pm-text-muted);
  font-size: 13px;
  margin-top: 20px;
  line-height: 1.6;
  opacity: 0.7;
}
</style>
