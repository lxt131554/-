<template>
  <el-container style="height:100vh">
    <!-- Sidebar -->
    <el-aside width="200px" class="layout-sidebar">
      <div class="sidebar-brand">
        <el-icon :size="20"><Monitor /></el-icon>
        <span>项目管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        background-color="transparent"
        text-color="#9B9DA0"
        active-text-color="#FFFFFF"
        router
        style="border-right:none;flex:1;padding-top:8px"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>
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
        <el-menu-item v-if="auth.user?.role=='admin'" index="/users">
          <el-icon><Setting /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item v-if="auth.user?.role=='leader'" index="/leader-dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>领导看板</span>
        </el-menu-item>
        <el-menu-item v-if="auth.user?.role=='leader'||auth.user?.role=='admin'" index="/statistics">
          <el-icon><TrendCharts /></el-icon>
          <span>统计分析</span>
        </el-menu-item>
        <el-menu-item index="/experiences">
          <el-icon><Collection /></el-icon>
          <span>经验库</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- Main area -->
    <el-container>
      <el-header class="layout-header">
        <el-popover ref="notifyPopover" placement="bottom-end" :width="320" trigger="click">
          <template #reference>
            <el-badge :value="notifications.length" :hidden="!notifications.length" :max="99" style="margin-right:20px;cursor:pointer">
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>
          </template>
          <div v-if="notifications.length" style="max-height:400px;overflow-y:auto">
            <div v-for="(n, i) in notifications" :key="i"
              style="padding:12px 8px;border-bottom:1px solid #f0f0f0;cursor:pointer"
              @click="goNotify(n.url)">
              <div style="font-size:14px;color:var(--pm-text)">{{ n.message }}</div>
              <div style="font-size:12px;color:#999;margin-top:4px">{{ n.time?.substring(0,16) }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无待办事项" :image-size="60" />
        </el-popover>
        <div class="header-user">
          <span class="header-name">{{ auth.user?.realName }}</span>
          <span class="header-role">{{ roleLabel }}</span>
          <span class="header-divider"></span>
          <el-button type="danger" text size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { computed, ref, onMounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import request from '../api/index'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const notifyPopover = ref(null)

const roleLabel = computed(() => {
  const map = { admin: '管理员', manager: '项目负责人', engineer: '工程师', leader: '院领导' }
  return map[auth.user?.role] || ''
})

const notifications = ref([])

async function loadNotifications() {
  try {
    const res = await request.get('/notifications')
    notifications.value = res.data || []
  } catch {}
}

function goNotify(url) {
  notifyPopover.value?.hide()
  router.push(url)
}

onMounted(() => { loadNotifications() })

function handleLogout() {
  auth.logout().then(() => router.push('/login'))
}
</script>

<style scoped>
/* Sidebar container -- not glued to edge */
.layout-sidebar {
  background: var(--pm-sidebar-bg);
  margin: 6px 0 6px 6px;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Brand area */
.sidebar-brand {
  padding: 22px 20px 18px;
  color: #FFFFFF;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.02em;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

/* Top header -- floating feel */
.layout-header {
  background: var(--pm-surface);
  border-bottom: 1px solid var(--pm-border);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 var(--pm-space-lg);
  height: 52px;
}

/* User info */
.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-name {
  color: var(--pm-text);
  font-size: 16px;
  font-weight: 500;
}

.header-role {
  color: var(--pm-text-muted);
  font-size: 13px;
  background: var(--pm-surface-hover);
  padding: 2px 10px;
  border-radius: 99px;
  font-weight: 500;
}

.header-divider {
  width: 1px;
  height: 16px;
  background: var(--pm-border);
  margin: 0 4px;
}

/* Main content */
.layout-main {
  background: var(--pm-bg);
  padding: var(--pm-space-lg);
  overflow-y: auto;
}
</style>
