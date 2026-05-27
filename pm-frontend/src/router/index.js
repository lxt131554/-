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
      { path: 'deviations', name: 'DeviationList', component: () => import('../views/DeviationList.vue') },
      { path: 'supports', name: 'SupportList', component: () => import('../views/SupportList.vue') },
      { path: 'supports/new', name: 'SupportForm', component: () => import('../views/SupportForm.vue') },
      { path: 'supports/:id', name: 'SupportEdit', component: () => import('../views/SupportForm.vue') },
      { path: 'projects', name: 'ProjectList', component: () => import('../views/ProjectList.vue') },
      { path: 'projects/:id', name: 'ProjectDetail', component: () => import('../views/ProjectDetail.vue') },
      { path: 'my-tasks', name: 'MyTasks', component: () => import('../views/MyTasks.vue') },
      { path: 'my-tasks/:stageId/report', name: 'StageReport', component: () => import('../views/StageReport.vue') },
      { path: 'pending-review', name: 'PendingReview', component: () => import('../views/PendingReview.vue') },
      { path: 'pending-review/:reportId', name: 'Review', component: () => import('../views/Review.vue') },
      { path: 'achievement-review/:reportId', name: 'AchievementReview', component: () => import('../views/AchievementReview.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// Route guard for auth
router.beforeEach(async (to, from, next) => {
  if (to.path === '/login') {
    next()
    return
  }
  const auth = useAuthStore()
  if (!auth.isLoggedIn) {
    try {
      await auth.fetchUser()
      next()
    } catch {
      next('/login')
    }
  } else {
    next()
  }
})

export default router
