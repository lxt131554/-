<template>
  <div class="page-container">
    <!-- Section 1: Page Header -->
    <section class="page-header">
      <h2>工作台</h2>
      <p style="color:var(--pm-text-secondary);margin-top:4px;font-size:16px">欢迎，{{ auth.user?.realName }}</p>
    </section>

    <!-- Pending Invitations -->
    <div v-if="pendingInvites.length" style="margin-bottom:16px">
      <el-card v-for="inv in pendingInvites" :key="inv.id" shadow="hover"
        style="display:flex;align-items:center;justify-content:space-between;padding:8px 16px;margin-bottom:8px;border:1px solid rgba(245,108,108,0.2)">
        <span>被邀请加入项目「{{ inv.projectName }}」作为{{ inv.roleInProject==='manager'?'负责人':'工程师' }}</span>
        <el-button type="primary" size="small" @click="handleAcceptInvite(inv)">接受</el-button>
      </el-card>
    </div>

    <!-- Section 2: Summary Cards -->
    <section class="page-summary-grid">
      <!-- Manager cards -->
      <template v-if="auth.user?.role=='manager'">
        <div class="summary-card summary-card--primary" @click="$router.push('/projects')">
          <div class="summary-card-value">{{ stats.myProjectCount ?? '--' }}</div>
          <div class="summary-card-label">我负责项目</div>
          <div class="summary-card-hint">当前跟进中的项目数量</div>
        </div>
        <div class="summary-card summary-card--primary" @click="$router.push('/pending-review')">
          <div class="summary-card-value">{{ stats.pendingReview ?? '--' }}</div>
          <div class="summary-card-label">待审核填报</div>
          <div class="summary-card-hint">需要处理的审核事项</div>
        </div>
        <div class="summary-card summary-card--warning" @click="$router.push('/deviations')">
          <div class="summary-card-value">{{ stats.openDeviations ?? '--' }}</div>
          <div class="summary-card-label">未关闭偏差</div>
          <div class="summary-card-hint">影响进度或成果质量的异常</div>
        </div>
        <div class="summary-card summary-card--danger" @click="$router.push('/supports')">
          <div class="summary-card-value">{{ stats.pendingSupports ?? '--' }}</div>
          <div class="summary-card-label">待处理支持事项</div>
          <div class="summary-card-hint">需协调解决的支持请求</div>
        </div>
        <div class="summary-card summary-card--success" @click="$router.push('/projects')">
          <div class="summary-card-value">{{ stats.nearDeadline ?? '--' }}</div>
          <div class="summary-card-label">本周临近节点</div>
          <div class="summary-card-hint">7天内计划完成阶段</div>
        </div>
      </template>

      <!-- Engineer cards -->
      <template v-if="auth.user?.role=='engineer'">
        <div class="summary-card summary-card--primary" @click="$router.push('/my-tasks')">
          <div class="summary-card-value">{{ stats.todo ?? '--' }}</div>
          <div class="summary-card-label">待填报阶段</div>
          <div class="summary-card-hint">需要提交进度汇报的阶段</div>
        </div>
        <div class="summary-card summary-card--warning" @click="$router.push('/my-tasks')">
          <div class="summary-card-value">{{ stats.returned ?? '--' }}</div>
          <div class="summary-card-label">被退回待补充</div>
          <div class="summary-card-hint">需修改后重新提交</div>
        </div>
        <div class="summary-card summary-card--danger" @click="$router.push('/my-tasks')">
          <div class="summary-card-value">{{ stats.overdue ?? '--' }}</div>
          <div class="summary-card-label">已逾期</div>
          <div class="summary-card-hint">超过计划完成时间</div>
        </div>
        <div class="summary-card summary-card--success" @click="$router.push('/my-tasks')">
          <div class="summary-card-value">{{ stats.nearDeadline ?? '--' }}</div>
          <div class="summary-card-label">本周临近节点</div>
          <div class="summary-card-hint">7天内计划完成阶段</div>
        </div>
      </template>

      <!-- Leader cards (kept as-is, unified style) -->
      <template v-if="auth.user?.role=='leader'">
        <div class="summary-card summary-card--warning" @click="$router.push('/deviations')">
          <div class="summary-card-value">{{ stats.openDeviations ?? '--' }}</div>
          <div class="summary-card-label">未关闭偏差</div>
          <div class="summary-card-hint">影响进度或成果质量的异常</div>
        </div>
        <div class="summary-card summary-card--danger" @click="$router.push('/supports')">
          <div class="summary-card-value">{{ stats.pendingSupports ?? '--' }}</div>
          <div class="summary-card-label">待处理支持事项</div>
          <div class="summary-card-hint">需协调解决的支持请求</div>
        </div>
        <div class="summary-card summary-card--primary" @click="$router.push('/pending-review')">
          <div class="summary-card-value">{{ stats.pendingReview ?? '--' }}</div>
          <div class="summary-card-label">待审阅填报</div>
          <div class="summary-card-hint">需要处理的审核事项</div>
        </div>
        <div class="summary-card summary-card--success" @click="$router.push('/leader-dashboard')">
          <div class="summary-card-value">{{ stats.pendingChanges ?? '--' }}</div>
          <div class="summary-card-label">待审批变更</div>
          <div class="summary-card-hint">待审批的变更申请</div>
        </div>
      </template>
    </section>

    <!-- Friendly empty state when all stats are 0 -->
    <div v-if="allStatsZero" style="text-align:center;padding:40px 0">
      <el-empty description="暂无待办事项，一切正常" :image-size="80" />
    </div>

    <!-- Section 3: Key Projects (manager & engineer only; leader view changes in Task 3) -->
    <section v-if="stats.myProjects && stats.myProjects.length" class="section-block">
      <div class="section-title">重点项目</div>
      <el-table v-if="stats.myProjects && stats.myProjects.length" :data="enrichedProjects" size="small" stripe
        style="width:100%">
        <el-table-column prop="name" label="项目名称" min-width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/projects/${row.projectId}`)">{{ row.projectName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="currentStage" label="当前阶段" min-width="120" />
        <el-table-column prop="stagePlanEnd" label="计划节点" min-width="120">
          <template #default="{ row }">
            <span v-if="row.stagePlanEnd">{{ formatDate(row.stagePlanEnd) }}</span>
            <span v-else style="color:var(--pm-text-muted)">—</span>
          </template>
        </el-table-column>
        <el-table-column label="风险状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.hasDeviation" type="danger" size="small">有偏差</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下一动作" min-width="100" align="center">
          <template #default="{ row }">
            <el-button v-if="row.nextAction==='审阅'" type="primary" size="small" link
              @click="$router.push('/pending-review')">审阅</el-button>
            <el-button v-else-if="row.nextAction==='填报'" type="primary" size="small" link
              @click="$router.push('/my-tasks')">填报</el-button>
            <el-button v-else type="primary" size="small" link
              @click="$router.push(`/projects/${row.projectId}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无项目数据" :image-size="60" />
    </section>

    <!-- Section 4: Tasks & Anomalies -->
    <section
      v-if="(auth.user?.role==='manager' && stats.pendingReviewItems && stats.pendingReviewItems.length) || (auth.user?.role==='engineer' && stats.pendingStages && stats.pendingStages.length)"
      class="section-block">
      <div class="section-title">待办与异常</div>

      <!-- Manager: pending review items -->
      <template v-if="auth.user?.role==='manager' && stats.pendingReviewItems">
        <el-table :data="stats.pendingReviewItems" size="small" stripe style="width:100%">
          <el-table-column prop="projectName" label="项目名称" min-width="160" />
          <el-table-column prop="stageName" label="阶段" min-width="120" />
          <el-table-column prop="submitUserName" label="提交人" min-width="100" />
          <el-table-column label="提交时间" min-width="160">
            <template #default="{ row }">
              <span v-if="row.submitTime">{{ formatDateTime(row.submitTime) }}</span>
              <span v-else style="color:var(--pm-text-muted)">—</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button type="primary" size="small" link
                @click="$router.push(`/projects/${row.projectId}`)">审阅</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- Engineer: pending stages -->
      <template v-if="auth.user?.role==='engineer' && stats.pendingStages">
        <el-table :data="stats.pendingStages" size="small" stripe style="width:100%">
          <el-table-column prop="projectName" label="项目名称" min-width="160" />
          <el-table-column prop="stageName" label="阶段" min-width="120" />
          <el-table-column label="计划完成" min-width="120">
            <template #default="{ row }">
              <span v-if="row.planEnd">{{ formatDate(row.planEnd) }}</span>
              <span v-else style="color:var(--pm-text-muted)">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status==='in_progress'" type="primary" size="small">进行中</el-tag>
              <el-tag v-else-if="row.status==='pending'" type="info" size="small">待开始</el-tag>
              <el-tag v-else size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button type="primary" size="small" link
                @click="$router.push(`/projects/${row.projectId}`)">填报</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import request from '../api/index'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const stats = ref({})
const pendingInvites = ref([])

const allStatsZero = computed(() => {
  const s = stats.value
  if (!s || Object.keys(s).length === 0) return false
  if (auth.user?.role === 'engineer') {
    return !s.todo && !s.returned && !s.overdue && !s.nearDeadline
  }
  if (auth.user?.role === 'manager') {
    return !s.pendingReview && !s.pendingAchievement && !s.pendingChanges && !s.openDeviations && !s.pendingSupports && !s.reviewOverdue && !s.nearDeadline
  }
  if (auth.user?.role === 'leader') {
    return !s.openDeviations && !s.pendingSupports && !s.pendingReview && !s.pendingChanges
  }
  return false
})

// Enrich project cards with computed nextAction based on role
const enrichedProjects = computed(() => {
  const projects = stats.value.myProjects || []
  if (auth.user?.role === 'engineer') {
    return projects.map(p => ({ ...p, nextAction: '填报' }))
  }
  // manager: check if project has pending review items
  const reviewItems = stats.value.pendingReviewItems || []
  const reviewProjectIds = new Set(reviewItems.map(r => r.projectId))
  return projects.map(p => ({
    ...p,
    nextAction: reviewProjectIds.has(p.projectId) ? '审阅' : '查看'
  }))
})

function formatDate(val) {
  if (!val) return ''
  const d = new Date(val)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatDateTime(val) {
  if (!val) return ''
  const d = new Date(val)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

async function loadStats() {
  try {
    const res = await request.get('/dashboard')
    stats.value = res.data
  } catch { }
}

async function loadPendingInvites() {
  try {
    const res = await request.get('/projects/members/pending')
    pendingInvites.value = res.data || []
  } catch { }
}

async function handleAcceptInvite(inv) {
  try {
    await request.put(`/projects/${inv.projectId}/members/${inv.id}/confirm`)
    ElMessage.success('已接受邀请')
    loadPendingInvites()
  } catch { }
}

onMounted(() => { loadStats(); loadPendingInvites() })
</script>

<style scoped>
/* Page-specific: all summary cards are clickable */
.summary-card {
  cursor: pointer;
}

/* Color accent variants via left border */
.summary-card--primary {
  border-left: 3px solid var(--pm-accent);
}
.summary-card--primary .summary-card-value {
  color: var(--pm-accent);
}

.summary-card--warning {
  border-left: 3px solid #F59E0B;
}
.summary-card--warning .summary-card-value {
  color: #F59E0B;
}

.summary-card--danger {
  border-left: 3px solid var(--pm-red-text);
}
.summary-card--danger .summary-card-value {
  color: var(--pm-red-text);
}

.summary-card--success {
  border-left: 3px solid var(--pm-green-text);
}
.summary-card--success .summary-card-value {
  color: var(--pm-green-text);
}
</style>
