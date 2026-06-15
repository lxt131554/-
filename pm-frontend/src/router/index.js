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
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'deviations', name: 'DeviationList', component: () => import('../views/DeviationList.vue'), meta: { roles: ['admin', 'manager', 'leader'] } },
      { path: 'deviations/:id', name: 'DeviationDetail', component: () => import('../views/DeviationDetail.vue'), meta: { roles: ['admin', 'manager', 'leader'] } },
      { path: 'supports', name: 'SupportList', component: () => import('../views/SupportList.vue') },
      { path: 'supports/new', name: 'SupportForm', component: () => import('../views/SupportForm.vue'), meta: { roles: ['admin', 'engineer'] } },
      { path: 'supports/:id', name: 'SupportEdit', component: () => import('../views/SupportForm.vue'), meta: { roles: ['admin', 'engineer', 'manager', 'leader'] } },
      { path: 'changes/:id', name: 'ChangeDetail', component: () => import('../views/ChangeDetail.vue'), meta: { roles: ['admin', 'manager', 'leader'] } },
      { path: 'projects', name: 'ProjectList', component: () => import('../views/ProjectList.vue'), meta: { roles: ['admin', 'manager', 'engineer', 'leader'] } },
      { path: 'projects/create', name: 'ProjectCreate', component: () => import('../views/ProjectCreate.vue'), meta: { roles: ['admin', 'manager'] } },
      { path: 'projects/:id', name: 'ProjectDetail', component: () => import('../views/ProjectDetail.vue'), meta: { roles: ['admin', 'manager', 'engineer', 'leader'] } },
      { path: 'stages/:id', name: 'StageDetail', component: () => import('../views/StageDetail.vue'), meta: { roles: ['admin', 'manager', 'engineer', 'leader'] } },
      { path: 'my-tasks', name: 'MyTasks', component: () => import('../views/MyTasks.vue'), meta: { roles: ['engineer'] } },
      { path: 'my-tasks/:stageId/report', name: 'StageReport', component: () => import('../views/StageReport.vue'), meta: { roles: ['engineer'] } },
      { path: 'pending-review', name: 'PendingReview', component: () => import('../views/PendingReview.vue'), meta: { roles: ['admin', 'manager'] } },
      { path: 'pending-review/:reportId', name: 'Review', component: () => import('../views/Review.vue'), meta: { roles: ['admin', 'manager'] } },
      { path: 'achievement-review/:reportId', name: 'AchievementReview', component: () => import('../views/AchievementReview.vue'), meta: { roles: ['admin', 'manager'] } },
      { path: 'leader-dashboard', name: 'LeaderDashboard', component: () => import('../views/LeaderDashboard.vue'), meta: { roles: ['admin', 'leader'] } },
      { path: 'projects/:id/review', name: 'ProjectReview', component: () => import('../views/ProjectReview.vue'), meta: { roles: ['admin', 'manager'] } },
      { path: 'projects/:id/experience', name: 'ProjectExperience', component: () => import('../views/ProjectExperience.vue'), meta: { roles: ['admin', 'manager'] } },
      { path: 'projects/:id/approval', name: 'ProjectApproval', component: () => import('../views/ProjectApproval.vue'), meta: { roles: ['admin', 'manager'] } },
      { path: 'experiences', name: 'ExperienceLibrary', component: () => import('../views/ExperienceLibrary.vue') },
      { path: 'users', name: 'UserManage', component: () => import('../views/UserManage.vue'), meta: { roles: ['admin'] } },
      { path: 'statistics', name: 'Statistics', component: () => import('../views/Statistics.vue'), meta: { roles: ['admin', 'leader'] } },
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

export default router
