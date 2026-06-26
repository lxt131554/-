<template>
  <el-container style="height:100vh">
    <!-- Sidebar -->
    <el-aside width="200px" class="layout-sidebar">
      <div class="sidebar-brand">
        <div class="brand-title">林草规划院项目管理系统</div>
        <div class="brand-subtitle">Project Lifecycle Platform</div>
      </div>
      <el-menu
        :default-active="route.path"
        background-color="transparent"
        text-color="#9B9DA0"
        active-text-color="#FFFFFF"
        router
        style="border-right:none;flex:1;padding-top:8px"
      >
        <!-- 工作台 -->
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <!-- 项目管理 -->
        <el-sub-menu index="sub-project">
          <template #title><el-icon><Folder /></el-icon><span>项目管理</span></template>
          <el-menu-item index="/projects">{{ auth.user?.role === 'leader' ? '全院项目' : '项目列表' }}</el-menu-item>
          <el-menu-item v-if="auth.user?.role=='manager'||auth.user?.role=='admin'" index="/projects/create">新建项目</el-menu-item>
        </el-sub-menu>

        <!-- 任务中心 -->
        <el-sub-menu index="sub-task" v-if="auth.user?.role=='engineer'||auth.user?.role=='manager'||auth.user?.role=='admin'">
          <template #title><el-icon><Edit /></el-icon><span>任务中心</span></template>
          <el-menu-item v-if="auth.user?.role=='engineer'" index="/my-tasks">我的待填报</el-menu-item>
          <el-menu-item v-if="auth.user?.role=='engineer'" index="/supports">发起支持申请</el-menu-item>
          <el-menu-item v-if="auth.user?.role=='manager'||auth.user?.role=='admin'" index="/pending-review">待审阅填报</el-menu-item>
        </el-sub-menu>

        <!-- 过程管控 -->
        <el-sub-menu index="sub-control" v-if="auth.user?.role=='manager'||auth.user?.role=='admin'||auth.user?.role=='leader'">
          <template #title><el-icon><Warning /></el-icon><span>过程管控</span></template>
          <el-menu-item index="/deviations">偏差台账</el-menu-item>
          <el-menu-item index="/supports">支持事项</el-menu-item>
        </el-sub-menu>

        <!-- 领导视图 -->
        <el-sub-menu index="sub-leader" v-if="auth.user?.role=='leader'||auth.user?.role=='admin'">
          <template #title><el-icon><DataAnalysis /></el-icon><span>领导视图</span></template>
          <el-menu-item index="/leader-dashboard">领导看板</el-menu-item>
          <el-menu-item index="/statistics">统计分析</el-menu-item>
        </el-sub-menu>

        <!-- 经验沉淀 -->
        <el-sub-menu index="sub-knowledge">
          <template #title><el-icon><Collection /></el-icon><span>经验沉淀</span></template>
          <el-menu-item index="/experiences">经验库</el-menu-item>
        </el-sub-menu>

        <!-- 系统管理 -->
        <el-sub-menu index="sub-system" v-if="auth.user?.role=='admin'">
          <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/users">用户管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- Main area -->
    <el-container>
      <el-header class="layout-header">
        <div class="header-location">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item.title" :to="item.path ? { path: item.path } : undefined">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
          <div class="header-page-title">{{ currentTitle }}</div>
        </div>
        <div class="header-actions">
          <el-popover ref="notifyPopover" placement="bottom-end" :width="320" trigger="click">
            <template #reference>
              <el-badge :value="notifications.length" :hidden="!notifications.length" :max="99" class="header-notify">
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
import { showActionError } from '../utils/actionGuards'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const notifyPopover = ref(null)

const roleLabel = computed(() => {
  const map = { admin: '管理员', manager: '项目负责人', engineer: '工程师', leader: '院领导' }
  return map[auth.user?.role] || ''
})

const notifications = ref([])

const currentTitle = computed(() => route.meta?.title || '工作台')
const breadcrumbItems = computed(() => {
  const items = [{ title: '首页', path: defaultPathForRole(auth.user?.role) }]
  if (route.meta?.parentTitle) {
    items.push({ title: route.meta.parentTitle, path: route.meta.parentPath })
  }
  if (currentTitle.value !== '工作台') {
    items.push({ title: currentTitle.value })
  }
  return items
})

function defaultPathForRole(role) {
  return role === 'leader' ? '/leader-dashboard' : '/dashboard'
}

async function loadNotifications() {
  try {
    const res = await request.get('/notifications')
    notifications.value = res.data || []
  } catch (error) {
    showActionError(error, '通知加载失败')
  }
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
  padding: 20px 20px 16px;
  color: #FFFFFF;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.brand-title {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.04em;
  line-height: 1.3;
}
.brand-subtitle {
  font-size: 11px;
  color: rgba(255,255,255,0.35);
  letter-spacing: 0.06em;
  margin-top: 2px;
}

/* Top header -- floating feel */
.layout-header {
  background: var(--pm-surface);
  border-bottom: 1px solid var(--pm-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 var(--pm-space-lg);
  height: 58px;
}

.header-location {
  min-width: 0;
}

.header-page-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--pm-text);
  line-height: 1.2;
  margin-top: 2px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.header-notify {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
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
