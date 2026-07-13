<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>领导看板</h2>
        <p style="color:var(--pm-text-secondary);margin-top:4px">全院项目总览</p>
      </div>
      <div v-if="auth.user?.role==='leader'||auth.user?.role==='admin'">
        <input ref="oaFileInput" type="file" accept=".xls,.xlsx" style="display:none" @change="handleOaFileSelected" />
        <el-button :loading="importingOa" @click="oaFileInput?.click()">
          <el-icon><Upload /></el-icon> 导入 OA 项目
        </el-button>
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
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" link size="small" @click="$router.push(row.linkUrl)">查看</el-button>
            </div>
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
      <div v-if="hasMoreAttention" style="text-align:right;margin-top:8px">
        <el-link type="primary" @click="$router.push('/projects')">仅展示前 8 个项目，查看全部 →</el-link>
      </div>
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
      <el-table v-if="allProjects.length" :data="pagedProjects" size="small" stripe style="width:100%" v-loading="loading">
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
      <el-empty v-if="!loading && !allProjects.length" description="暂无项目" :image-size="60" />
      <el-pagination
        v-model:current-page="projectPage" :page-size="pageSize"
        :total="allProjects.length" layout="total, prev, pager, next" :pager-count="5" size="small"
        style="margin-top:12px;justify-content:flex-end" />
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
const oaFileInput = ref(null)
const importingOa = ref(false)

async function handleOaFileSelected(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  importingOa.value = true
  try {
    const { importProjectsFromOa } = await import('../api/project')
    const { ElMessageBox } = await import('element-plus')
    const res = await importProjectsFromOa(file)
    const data = res.data || {}
    const missing = data.missingManagers?.length ? data.missingManagers.join('、') : '无'
    const items = data.items || []
    const skippedItems = items.filter(i => i.action === 'skipped')
    let detailLines = []
    if (skippedItems.length > 0) {
      detailLines.push('')
      detailLines.push('⚠️ 以下行导入失败：')
      skippedItems.forEach(i => {
        detailLines.push(`第 ${i.rowNumber} 行 「${i.projectName || '未知'}」编号 ${i.contractNo || '-'} 负责人 ${i.managerName || '-'} —— ${i.message}`)
      })
    }
    await ElMessageBox.alert(
      `读取项目：${data.totalRows || 0} 条\n新增：${data.createdCount || 0} 条\n更新：${data.updatedCount || 0} 条\n跳过：${data.skippedCount || 0} 条\n负责人已匹配：${data.matchedManagerCount || 0} 条\n未匹配负责人：${missing}${detailLines.join('\n')}`,
      'OA 项目导入结果',
      { confirmButtonText: '知道了', type: (skippedItems.length > 0 || data.updatedCount > 0) ? 'warning' : 'success', dangerouslyUseHTMLString: false }
    )
    await loadData()
  } catch (error) {
    const msg = (error?.response?.data?.message || error?.message || 'OA 项目导入失败').replace(/\n/g, '<br>')
    await ElMessageBox.alert(msg, 'OA 项目导入失败', { confirmButtonText: '知道了', type: 'error', dangerouslyUseHTMLString: true })
  } finally { importingOa.value = false }
}

const projectPage = ref(1)
const pageSize = 10
const allProjects = computed(() => stats.value.projects || [])
const pagedProjects = computed(() => {
  const start = (projectPage.value - 1) * pageSize
  return allProjects.value.slice(start, start + pageSize)
})

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
  ].slice(0, 8)
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
  }).slice(0, 8)
})

const hasMoreAttention = computed(() => {
  const projects = stats.value.projects || []
  const deviations = stats.value.openDeviationList || []
  const supports = stats.value.pendingSupportList || []
  const changes = stats.value.pendingChanges || []
  if (!projects.length) return false
  const count = projects.filter(p => {
    return deviations.some(d => d.projectId === p.id) ||
      supports.some(s => s.projectId === p.id) ||
      changes.some(c => c.projectId === p.id)
  }).length
  return count > 8
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
