<template>
  <el-container style="height:100vh">
    <!-- Sidebar -->
    <el-aside width="248px" class="layout-sidebar">
      <div class="sidebar-brand">
        <div class="brand-logo"><el-icon :size="24"><OfficeBuilding /></el-icon></div>
        <div class="brand-copy">
          <div class="brand-title">四川省林业和草原调查规划院</div>
          <div class="brand-subtitle">项目全过程管理系统</div>
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
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item.title" :to="item.path ? { path: item.path } : undefined">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- Theme picker -->
          <el-dropdown trigger="click" @command="switchTheme">
            <span class="header-action-btn" title="切换主题">
              <el-icon :size="18"><Brush /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="blue"><span class="theme-dot theme-dot--blue"></span> 政务蓝</el-dropdown-item>
                <el-dropdown-item command="purple"><span class="theme-dot theme-dot--purple"></span> 典雅紫</el-dropdown-item>
                <el-dropdown-item command="green"><span class="theme-dot theme-dot--green"></span> 生态绿</el-dropdown-item>
                <el-dropdown-item command="dark"><span class="theme-dot theme-dot--dark"></span> 深色蓝</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- Notifications -->
          <el-popover ref="notifyPopover" placement="bottom-end" :width="320" trigger="click">
            <template #reference>
              <span class="header-action-btn">
                <el-badge :value="notifications.length" :hidden="!notifications.length" :max="99">
                  <el-icon :size="18"><Bell /></el-icon>
                </el-badge>
              </span>
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

          <!-- User -->
          <div class="header-user">
            <span class="header-avatar">{{ userInitial }}</span>
            <span class="header-name" style="cursor:pointer" @click="showProfile=true">{{ auth.user?.realName }}</span>
            <span class="header-role">{{ roleLabel }}</span>
            <span class="header-divider"></span>
            <el-button text size="small" @click="showProfile=true">修改密码</el-button>
            <el-button type="danger" text size="small" @click="handleLogout">退出</el-button>
          </div>
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
import { Bell, Brush } from '@element-plus/icons-vue'
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

const theme = ref(localStorage.getItem('pm-theme') || 'blue')
function switchTheme(name) {
  theme.value = name
  document.documentElement.setAttribute('data-theme', name)
  localStorage.setItem('pm-theme', name)
}

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

onMounted(() => {
  document.documentElement.setAttribute('data-theme', theme.value)
  loadNotifications()
})

function handleLogout() {
  auth.logout().then(() => router.push('/login'))
}
</script>

<style scoped>
.layout-sidebar {
  background: var(--pm-sidebar-bg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar-brand {
  padding: 20px 20px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.brand-logo { color: #fff; }
.brand-title { font-size: 14px; font-weight: 600; color: #fff; line-height: 1.4; letter-spacing: 0.02em; }
.brand-subtitle { font-size: 11px; color: rgba(255,255,255,0.45); margin-top: 2px; letter-spacing: 0.04em; }
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
  padding: 0 24px;
  height: 56px;
}
.header-left { display: flex; align-items: center; }
.header-right { display: flex; align-items: center; gap: 4px; }
.header-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--pm-text-secondary);
  transition: background 0.15s;
}
.header-action-btn:hover { background: var(--pm-surface-hover); color: var(--pm-text); }
.header-user { display: flex; align-items: center; gap: 8px; margin-left: 8px; }
.header-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--pm-primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600;
}
.header-name { color: var(--pm-text); font-size: 14px; font-weight: 500; }
.header-role { color: var(--pm-text-muted); font-size: 12px; background: var(--pm-surface-hover); padding: 2px 8px; border-radius: 99px; }
.header-divider { width: 1px; height: 16px; background: var(--pm-border); }
.theme-dot { display: inline-block; width: 12px; height: 12px; border-radius: 50%; margin-right: 8px; vertical-align: middle; }
.theme-dot--blue { background: #2563eb; }
.theme-dot--purple { background: #7c3aed; }
.theme-dot--green { background: #059669; }
.theme-dot--dark { background: #0f172a; }

/* Main content */
.layout-main {
  background: var(--pm-bg);
  padding: 0;
  overflow-y: auto;
}
</style>
