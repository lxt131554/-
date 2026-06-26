<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>领导看板</h2>
        <p style="color:var(--pm-text-secondary);margin-top:4px">全院项目总览</p>
      </div>
    </div>

    <!-- Tier 1: 全院概览 — summary cards -->
    <section class="page-summary-grid">
      <div class="summary-card summary-card--primary clickable" @click="$router.push('/projects')">
        <div class="summary-card-value">{{ stats.activeProjects ?? '--' }}</div>
        <div class="summary-card-label">在建项目</div>
        <div class="summary-card-hint">当前执行中的项目数量</div>
      </div>
      <div class="summary-card summary-card--danger clickable" @click="$router.push('/deviations')">
        <div class="summary-card-value">{{ highRiskProjectCount }}</div>
        <div class="summary-card-label">高风险项目</div>
        <div class="summary-card-hint">存在未关闭偏差的项目</div>
      </div>
      <div class="summary-card summary-card--warning" @click="$router.push('/deviations')">
        <div class="summary-card-value">{{ stats.openDeviations ?? '--' }}</div>
        <div class="summary-card-label">未关闭偏差</div>
        <div class="summary-card-hint">影响进度或质量的异常事项</div>
      </div>
      <div class="summary-card summary-card--danger" @click="$router.push('/supports')">
        <div class="summary-card-value">{{ stats.pendingSupports ?? '--' }}</div>
        <div class="summary-card-label">待处理支持事项</div>
        <div class="summary-card-hint">需院级协调解决的事项</div>
      </div>
      <div class="summary-card summary-card--success clickable" @click="$router.push('/projects')">
        <div class="summary-card-value">{{ stats.completedProjects ?? '--' }}</div>
        <div class="summary-card-label">已结项项目</div>
        <div class="summary-card-hint">已完成并归档的项目</div>
      </div>
    </section>

    <!-- Tier 2: 重点风险事项 — combined deviations + supports + changes -->
    <section class="section-block">
      <div class="section-title">重点风险事项</div>
      <el-table v-if="riskItems.length" :data="riskItems" size="small" stripe style="width:100%">
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type==='偏差'" type="danger" size="small">偏差</el-tag>
            <el-tag v-else-if="row.type==='支持'" type="warning" size="small">支持</el-tag>
            <el-tag v-else type="info" size="small">变更</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="所属项目" min-width="160" />
        <el-table-column prop="summary" label="事项概要" min-width="280" show-overflow-tooltip />
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="$router.push(row.linkUrl)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无风险事项" :image-size="60" />
    </section>

    <!-- Tier 3: 需关注项目 — THE MOST IMPORTANT section -->
    <section class="section-block">
      <div class="section-title">需关注项目</div>
      <el-table v-if="attentionProjects.length" :data="attentionProjects" size="small" stripe style="width:100%">
        <el-table-column prop="name" label="项目名称" min-width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/projects/${row.id}`)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="issue" label="当前主要问题" min-width="280" show-overflow-tooltip />
        <el-table-column prop="owner" label="负责人" min-width="100" />
        <el-table-column prop="action" label="建议领导关注点" min-width="240" show-overflow-tooltip />
      </el-table>
      <el-empty v-else description="暂无项目需要关注" :image-size="60" />
    </section>

    <!-- All projects — compact table at bottom -->
    <div class="section-block">
      <div class="section-title">
        全院项目
        <el-button text type="primary" size="small" style="float:right" @click="$router.push('/projects')">
          查看全部 →
        </el-button>
      </div>
      <el-table v-if="allProjects.length" :data="allProjects" size="small" stripe style="width:100%" v-loading="loading">
        <el-table-column prop="name" label="项目名称" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/projects/${row.id}`)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status==='active'?'success':'info'" size="small">{{ row.status==='active'?'进行中':'已完成' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="负责人" min-width="100" />
        <el-table-column prop="currentStageName" label="当前阶段" min-width="120" />
      </el-table>
      <el-empty v-else-if="!loading" description="暂无项目" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../api/index'
import { useAuthStore } from '../stores/auth'
import { showActionError } from '../utils/actionGuards'

const stats = ref({})
const loading = ref(false)
const auth = useAuthStore()

const allProjects = computed(() => stats.value.projects || [])

// Count projects that have at least one open deviation
const highRiskProjectCount = computed(() => {
  const deviations = stats.value.openDeviationList || []
  const projectIds = new Set(deviations.map(d => d.projectId))
  return projectIds.size
})

// Tier 2: merge deviations (limit 3) + supports (limit 3) + changes (limit 2)
const riskItems = computed(() => {
  const deviations = (stats.value.openDeviationList || []).map(d => ({
    type: '偏差',
    projectName: d.projectName || '',
    summary: d.description || '',
    createTime: d.createTime || '',
    linkUrl: `/deviations/${d.id}`
  }))
  const supports = (stats.value.pendingSupportList || []).map(s => ({
    type: '支持',
    projectName: s.projectName || '',
    summary: s.title || '',
    createTime: s.createTime || '',
    linkUrl: `/supports/${s.id}`
  }))
  const changes = (stats.value.pendingChanges || []).map(c => ({
    type: '变更',
    projectName: c.projectName || '',
    summary: c.content || '',
    createTime: c.confirmTime || c.createTime || '',
    linkUrl: `/changes/${c.id}`
  }))
  return [
    ...deviations.slice(0, 3),
    ...supports.slice(0, 3),
    ...changes.slice(0, 2)
  ]
})

// Tier 3: projects needing leader attention
const attentionProjects = computed(() => {
  const projects = stats.value.projects || []
  const deviations = stats.value.openDeviationList || []
  const supports = stats.value.pendingSupportList || []
  const changes = stats.value.pendingChanges || []

  if (!projects.length) return []

  return projects.filter(p => {
    return deviations.some(d => d.projectId === p.id) ||
      supports.some(s => s.projectId === p.id) ||
      changes.some(c => c.projectId === p.id)
  }).map(p => {
    const dev = deviations.find(d => d.projectId === p.id)
    const sup = supports.find(s => s.projectId === p.id)
    const chg = changes.find(c => c.projectId === p.id)

    let issue = ''
    let action = ''

    if (dev) {
      issue = dev.description || ''
      action = '关注偏差处理进度，必要时协调资源'
    } else if (sup) {
      issue = sup.title || ''
      action = '关注支持事项处理进度'
    } else if (chg) {
      issue = chg.content || ''
      action = '关注变更审批进度'
    }

    return {
      id: p.id,
      name: p.name,
      issue,
      owner: p.managerName || '—',
      action
    }
  })
})

function formatTime(val) {
  if (!val) return '-'
  return val.substring(0, 16).replace('T', ' ')
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/leader-dashboard')
    stats.value = res.data
  } catch (error) {
    showActionError(error, '领导看板加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

/* Clickable cards get pointer cursor */
.summary-card {
  cursor: default;
}
.summary-card.clickable,
.summary-card[class*='summary-card--warning'],
.summary-card[class*='summary-card--danger'] {
  cursor: pointer;
}

/* Color accent left-border variants — matching Dashboard.vue pattern */
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
