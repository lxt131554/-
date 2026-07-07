import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'deviations', name: 'DeviationList', component: () => import('../views/DeviationList.vue'), meta: { title: '偏差台账', roles: ['admin', 'manager', 'leader'] } },
      { path: 'deviations/:id', name: 'DeviationDetail', component: () => import('../views/DeviationDetail.vue'), meta: { title: '偏差详情', parentTitle: '偏差台账', parentPath: '/deviations', roles: ['admin', 'manager', 'leader'] } },
      { path: 'supports', name: 'SupportList', component: () => import('../views/SupportList.vue'), meta: { title: '支持事项' } },
      { path: 'supports/new', name: 'SupportForm', component: () => import('../views/SupportForm.vue'), meta: { title: '新建支持申请', parentTitle: '支持事项', parentPath: '/supports', roles: ['admin', 'engineer'] } },
      { path: 'supports/:id', name: 'SupportEdit', component: () => import('../views/SupportForm.vue'), meta: { title: '支持事项详情', parentTitle: '支持事项', parentPath: '/supports', roles: ['admin', 'engineer', 'manager', 'leader'] } },
      { path: 'changes/:id', name: 'ChangeDetail', component: () => import('../views/ChangeDetail.vue'), meta: { title: '变更详情', roles: ['admin', 'manager', 'leader'] } },
      { path: 'projects', name: 'ProjectList', component: () => import('../views/ProjectList.vue'), meta: { title: '项目列表', roles: ['admin', 'manager', 'engineer', 'leader'] } },
      { path: 'projects/create', name: 'ProjectCreate', component: () => import('../views/ProjectCreate.vue'), meta: { title: '新建项目', parentTitle: '项目列表', parentPath: '/projects', roles: ['admin', 'manager'] } },
      { path: 'projects/:id', name: 'ProjectDetail', component: () => import('../views/ProjectDetail.vue'), meta: { title: '项目详情', parentTitle: '项目列表', parentPath: '/projects', roles: ['admin', 'manager', 'engineer', 'leader'] } },
      { path: 'stages/:id', name: 'StageDetail', component: () => import('../views/StageDetail.vue'), meta: { title: '阶段详情', roles: ['admin', 'manager', 'engineer', 'leader'] } },
      { path: 'my-tasks', name: 'MyTasks', component: () => import('../views/MyTasks.vue'), meta: { title: '我的待填报', roles: ['engineer', 'manager', 'admin'] } },
      { path: 'my-tasks/:stageId/report', name: 'StageReport', component: () => import('../views/StageReport.vue'), meta: { title: '阶段填报', parentTitle: '我的待填报', parentPath: '/my-tasks', roles: ['engineer', 'manager', 'admin'] } },
      { path: 'pending-review', name: 'PendingReview', component: () => import('../views/PendingReview.vue'), meta: { title: '待审阅填报', roles: ['admin', 'manager'] } },
      { path: 'pending-review/:reportId', name: 'Review', component: () => import('../views/Review.vue'), meta: { title: '填报审阅', parentTitle: '待审阅填报', parentPath: '/pending-review', roles: ['admin', 'manager'] } },
      { path: 'achievement-review/:reportId', name: 'AchievementReview', component: () => import('../views/AchievementReview.vue'), meta: { title: '成果审核', parentTitle: '待审阅填报', parentPath: '/pending-review', roles: ['admin', 'manager'] } },
      { path: 'leader-dashboard', name: 'LeaderDashboard', component: () => import('../views/LeaderDashboard.vue'), meta: { title: '领导看板', roles: ['admin', 'leader'] } },
      { path: 'projects/:id/review', name: 'ProjectReview', component: () => import('../views/ProjectReview.vue'), meta: { title: '项目自评', parentTitle: '项目详情', roles: ['admin', 'manager'] } },
      { path: 'projects/:id/experience', name: 'ProjectExperience', component: () => import('../views/ProjectExperience.vue'), meta: { title: '经验总结', parentTitle: '项目详情', roles: ['admin', 'manager'] } },
      { path: 'projects/:id/approval', name: 'ProjectApproval', component: () => import('../views/ProjectApproval.vue'), meta: { title: '成果评审', parentTitle: '项目详情', roles: ['admin', 'manager'] } },
      { path: 'experiences', name: 'ExperienceLibrary', component: () => import('../views/ExperienceLibrary.vue'), meta: { title: '经验库' } },
      { path: 'users', name: 'UserManage', component: () => import('../views/UserManage.vue'), meta: { title: '用户管理', roles: ['admin'] } },
      { path: 'statistics', name: 'Statistics', component: () => import('../views/Statistics.vue'), meta: { title: '统计分析', roles: ['admin', 'leader'] } },
      { path: 'invitations', redirect: '/dashboard' },
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

function defaultPathForRole(role) {
  return role === 'leader' ? '/leader-dashboard' : '/dashboard'
}

function canEnter(to, role) {
  const roles = to.meta?.roles
  return !roles || roles.includes(role)
}

// Route guard for auth and role-level page access
router.beforeEach(async (to, from, next) => {
  if (to.path === '/login') {
    next()
    return
  }
  const auth = useAuthStore()
  if (!auth.isLoggedIn) {
    try {
      await auth.fetchUser()
    } catch {
      next('/login')
      return
    }
  }
  if (!canEnter(to, auth.user?.role)) {
    next(defaultPathForRole(auth.user?.role))
    return
  }
  next()
})

router.afterEach((to) => {
  document.title = `${to.meta?.title || '工作台'} - 林草规划院项目管理系统`
})

export default router
