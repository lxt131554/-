<template>
  <el-container style="height:100vh">
    <el-aside width="220px" style="background:var(--pm-sidebar-bg);overflow:hidden">
      <div style="padding:20px;text-align:center;color:#fff;font-size:18px;font-weight:600;border-bottom:1px solid rgba(255,255,255,0.1)">
        <el-icon style="margin-right:6px"><Monitor /></el-icon>
        项目管理系统
      </div>
      <el-menu
        :default-active="route.path"
        background-color="#1a3a5c"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
        style="border-right:none"
      >
        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <span>项目列表</span>
        </el-menu-item>
        <el-menu-item v-if="auth.user?.role==='engineer'" index="/my-tasks">
          <el-icon><Edit /></el-icon>
          <span>我的待填报</span>
        </el-menu-item>
        <el-menu-item v-if="auth.user?.role==='manager'" index="/pending-review">
          <el-icon><View /></el-icon>
          <span>待审阅</span>
        </el-menu-item>
        <el-sub-menu index="sub-process" v-if="auth.user?.role=='manager'||auth.user?.role=='admin'">
          <template #title>
            <el-icon><Warning /></el-icon>
            <span>过程管理</span>
          </template>
          <el-menu-item index="/deviations">
            <el-icon><DataAnalysis /></el-icon>
            <span>偏差台账</span>
          </el-menu-item>
          <el-menu-item index="/supports">
            <el-icon><Headset /></el-icon>
            <span>支持事项</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-if="auth.user?.role=='engineer'" index="/supports">
          <el-icon><Headset /></el-icon>
          <span>支持事项</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="background:#fff;border-bottom:1px solid var(--pm-border);display:flex;align-items:center;justify-content:flex-end;padding:0 24px">
        <span style="margin-right:12px;color:var(--pm-text-secondary)">
          {{ auth.user?.realName }}（{{ roleLabel }}）
        </span>
        <el-button type="danger" text @click="handleLogout">退出</el-button>
      </el-header>
      <el-main style="background:var(--pm-bg);padding:20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const roleLabel = computed(() => {
  const map = { admin: '管理员', manager: '项目负责人', engineer: '工程师', leader: '院领导' }
  return map[auth.user?.role] || ''
})

function handleLogout() {
  auth.logout().then(() => router.push('/login'))
}
</script>
