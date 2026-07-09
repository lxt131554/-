<template>
  <main class="login-page">
    <section class="login-visual">
      <img src="../assets/login-forest-landscape.webp" alt="四川林草生态规划景观" />
      <div class="visual-shade"></div>
      <div class="visual-content">
        <div class="visual-brand">
          <span class="brand-mark"><el-icon><OfficeBuilding /></el-icon></span>
          <span>四川省林业和草原调查规划院</span>
        </div>
        <h1>项目管理系统</h1>
        <p>项目全周期协同、过程管控与成果沉淀</p>
      </div>
    </section>

    <section class="login-form-side">
      <div class="login-panel">
        <header class="login-header">
          <span class="login-eyebrow">PROJECT MANAGEMENT</span>
          <h2>欢迎登录</h2>
          <p>请使用院内系统账号进入工作台</p>
        </header>
        <el-form :model="form" :rules="rules" ref="formRef" size="large">
          <el-form-item>
            <div style="margin-bottom:4px;font-size:14px;font-weight:500">用户名</div>
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
          </el-form-item>
          <el-form-item>
            <div style="margin-bottom:4px;font-size:14px;font-weight:500">密码</div>
            <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password />
          </el-form-item>
          <el-form-item class="login-action">
            <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
          </el-form-item>
        </el-form>
        <div class="test-hint">
          <el-icon><InfoFilled /></el-icon>
          <span>测试账号：manager1 / engineer1 / leader1 / admin，密码均为 123456</span>
        </div>
      </div>
      <div class="login-footer">四川省林业和草原调查规划院</div>
    </section>
  </main>
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
  } catch (error) {
    const msg = error?.response?.data?.message || error?.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 58%) minmax(420px, 42%);
  background: #fff;
}

.login-visual {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.login-visual > img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: 48% center;
  display: block;
}

.visual-shade {
  position: absolute;
  inset: 0;
  background: rgba(8, 31, 24, 0.38);
}

.visual-content {
  position: absolute;
  left: clamp(32px, 5vw, 76px);
  right: clamp(32px, 5vw, 76px);
  bottom: clamp(48px, 9vh, 100px);
  color: #fff;
}

.visual-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 22px;
  font-size: 15px;
  font-weight: 500;
}

.brand-mark {
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: rgba(255,255,255,0.94);
  color: #1677ff;
  font-size: 18px;
}

.visual-content h1 {
  max-width: 680px;
  margin: 0;
  font-size: clamp(32px, 3.1vw, 48px);
  font-weight: 600;
  line-height: 1.25;
  letter-spacing: 0;
}

.visual-content > p {
  margin-top: 12px;
  color: rgba(255,255,255,0.82);
  font-size: 16px;
  letter-spacing: 0;
}

.login-form-side {
  min-height: 100vh;
  padding: 48px clamp(40px, 5vw, 76px) 28px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;
}

.login-panel {
  width: min(100%, 400px);
  margin: auto;
}

.login-header {
  margin-bottom: 34px;
}

.login-eyebrow {
  display: block;
  margin-bottom: 10px;
  color: var(--pm-primary);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.login-header h2 {
  margin: 0;
  color: var(--pm-text);
  font-size: 28px;
  line-height: 1.35;
  font-weight: 600;
  letter-spacing: 0;
}

.login-header p {
  margin-top: 6px;
  color: var(--pm-text-secondary);
  font-size: 14px;
}

.login-panel :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-panel :deep(.el-input__wrapper) {
  min-height: 44px;
}

.login-action {
  margin-top: 28px;
  margin-bottom: 0;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0;
}

.test-hint {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 18px;
  padding: 10px 12px;
  border: 1px solid #e5e8eb;
  border-radius: 6px;
  background: #f7f8fa;
  color: var(--pm-text-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.test-hint .el-icon {
  margin-top: 3px;
  color: var(--pm-primary);
  flex-shrink: 0;
}

.login-footer {
  width: min(100%, 400px);
  margin: 36px auto 0;
  color: var(--pm-text-muted);
  font-size: 12px;
  text-align: center;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    min-height: 260px;
    height: 34vh;
  }

  .visual-content {
    left: 24px;
    right: 24px;
    bottom: 28px;
  }

  .visual-brand {
    margin-bottom: 12px;
  }

  .visual-content h1 {
    font-size: 30px;
  }

  .visual-content > p {
    font-size: 14px;
  }

  .login-form-side {
    min-height: auto;
    padding: 38px 24px 24px;
  }
}

@media (max-width: 480px) {
  .login-visual {
    min-height: 220px;
    height: 30vh;
  }

  .visual-content > p,
  .login-eyebrow {
    display: none;
  }

  .login-header {
    margin-bottom: 26px;
  }
}
</style>
