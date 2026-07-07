<template>
  <el-container style="height:100vh">
    <!-- Sidebar -->
    <el-aside width="220px" class="layout-sidebar">
      <div class="sidebar-brand">
        <div class="brand-mark"><el-icon><OfficeBuilding /></el-icon></div>
        <div class="brand-copy">
          <div class="brand-title">林草规划院</div>
          <div class="brand-subtitle">项目管理系统</div>
        </div>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="sidebar-menu"
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
          <el-menu-item v-if="auth.user?.role=='engineer'||auth.user?.role=='manager'||auth.user?.role=='admin'" index="/my-tasks">我的待填报</el-menu-item>
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
                style="padding:10px 8px;border-bottom:1px solid #f0f0f0;cursor:pointer"
                @click="goNotify(n.url)">
                <div style="font-size:13px;font-weight:500;color:var(--pm-text)" v-if="n.projectName">{{ n.projectName }}</div>
                <div style="font-size:14px;color:var(--pm-text);margin:2px 0">{{ n.message }}</div>
                <div style="font-size:12px;color:var(--pm-text-muted)">{{ n.time }}</div>
              </div>
            </div>
            <el-empty v-else description="暂无待办事项" :image-size="60" />
          </el-popover>
          <div class="header-user">
            <span class="header-avatar">{{ userInitial }}</span>
            <span class="header-name" style="cursor:pointer" @click="showProfile=true">{{ auth.user?.realName }}</span>
            <span class="header-role">{{ roleLabel }}</span>
            <span class="header-divider"></span>
            <el-button text size="small" @click="showProfile=true">修改密码</el-button>
            <el-button type="danger" text size="small" @click="handleLogout">退出</el-button>
          </div>

          <!-- Profile Dialog -->
          <el-dialog v-model="showProfile" title="修改密码" width="400px" append-to-body align-center :lock-scroll="true">
            <el-form label-width="100px">
              <el-form-item label="旧密码">
                <el-input v-model="profileForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="profileForm.newPassword" type="password" show-password placeholder="至少6位" />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input v-model="profileForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button type="primary" @click="handleChangePassword" :loading="changingPwd">确认修改</el-button>
              <el-button @click="showProfile=false">取消</el-button>
            </template>
          </el-dialog>
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
import { computed, ref, reactive, onMounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../api/index'
import { showActionError } from '../utils/actionGuards'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const notifyPopover = ref(null)

// Profile / password change
const showProfile = ref(false)
const changingPwd = ref(false)
const profileForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
async function handleChangePassword() {
  if (!profileForm.oldPassword || !profileForm.newPassword) {
    ElMessage.warning('请输入旧密码和新密码')
    return
  }
  changingPwd.value = true
  try {
    await request.put('/auth/profile', { ...profileForm })
    ElMessage.success('密码修改成功')
    showProfile.value = false
    profileForm.oldPassword = ''
    profileForm.newPassword = ''
    profileForm.confirmPassword = ''
  } catch (error) {
    showActionError(error, '密码修改失败')
  } finally { changingPwd.value = false }
}

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

const userInitial = computed(() => auth.user?.realName?.slice(0, 1) || '用')

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
.layout-sidebar {
  background: var(--pm-sidebar-bg);
  border-right: 1px solid var(--pm-border);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(31, 35, 41, 0.03);
}

.sidebar-brand {
  height: 58px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--pm-text);
  border-bottom: 1px solid var(--pm-border-light);
  flex-shrink: 0;
}
.brand-mark {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: var(--pm-primary);
  color: #fff;
  font-size: 17px;
}
.brand-copy {
  min-width: 0;
}
.brand-title {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0;
  line-height: 1.25;
}
.brand-subtitle {
  font-size: 12px;
  color: var(--pm-text-muted);
  letter-spacing: 0;
  line-height: 1.25;
}
.sidebar-menu {
  border-right: none;
  flex: 1;
  padding: 10px 8px;
  overflow-y: auto;
}

.layout-header {
  background: var(--pm-surface);
  border-bottom: 1px solid var(--pm-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 24px;
  height: 56px;
  box-shadow: 0 1px 4px rgba(31, 35, 41, 0.03);
  z-index: 2;
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

.header-notify :deep(.el-icon) {
  color: var(--pm-text-secondary);
}

/* User info */
.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-name {
  color: var(--pm-text);
  font-size: 14px;
  font-weight: 500;
}

.header-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #e8f1ff;
  color: var(--pm-primary);
  font-size: 13px;
  font-weight: 600;
}

.header-role {
  color: var(--pm-text-muted);
  font-size: 12px;
  background: var(--pm-surface-hover);
  padding: 2px 8px;
  border-radius: 4px;
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
  padding: 24px;
  overflow-y: auto;
}

@media (max-width: 960px) {
  .layout-main {
    padding: 16px;
  }
}
</style>
