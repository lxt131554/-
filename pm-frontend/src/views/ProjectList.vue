<template>
  <div class="page-container project-list-page">
    <header class="project-page-heading">
      <div>
        <h2>项目列表</h2>
        <p>{{ auth.user?.role === 'leader' ? '查看全院项目进度、负责人和当前阶段' : '集中管理项目进度、负责人和执行状态' }}</p>
      </div>
      <div class="heading-actions">
        <input ref="oaFileInput" class="hidden-file-input" type="file" accept=".xls,.xlsx" @change="handleOaFileSelected" />
        <el-button :loading="importingOa" @click="triggerOaImport" v-if="auth.user?.role==='admin'">
          <el-icon><Upload /></el-icon><span>导入 OA 项目</span>
        </el-button>
        <el-button type="primary" @click="router.push('/projects/create')" v-if="auth.user?.role==='manager'||auth.user?.role==='admin'">
          <el-icon><Plus /></el-icon><span>新建项目</span>
        </el-button>
      </div>
    </header>

    <section class="project-summary-grid">
      <button class="project-summary-item" :class="{ active: statusFilter==='' }" @click="setStatusFilter('')">
        <span class="summary-icon summary-icon--blue"><el-icon><Files /></el-icon></span>
        <span class="summary-copy">
          <span class="summary-label">全部项目</span>
          <strong>{{ summaryTotal ?? '--' }}</strong>
        </span>
      </button>
      <button class="project-summary-item" :class="{ active: statusFilter==='active' }" @click="setStatusFilter('active')">
        <span class="summary-icon summary-icon--green"><el-icon><VideoPlay /></el-icon></span>
        <span class="summary-copy">
          <span class="summary-label">进行中</span>
          <strong>{{ activeCount ?? '--' }}</strong>
        </span>
      </button>
      <button class="project-summary-item" :class="{ active: statusFilter==='completed' }" @click="setStatusFilter('completed')">
        <span class="summary-icon summary-icon--gray"><el-icon><CircleCheck /></el-icon></span>
        <span class="summary-copy">
          <span class="summary-label">已完成</span>
          <strong>{{ completedCount ?? '--' }}</strong>
        </span>
      </button>
    </section>

    <section class="section-block project-table-panel" v-loading="loading">
      <div class="filter-bar">
        <el-input v-model="keyword" placeholder="搜索项目名称" clearable class="project-search" @clear="loadData" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="项目状态" clearable class="status-select" @change="loadData">
          <el-option label="全部状态" value="" />
          <el-option label="进行中" value="active" />
          <el-option label="已完成" value="completed" />
        </el-select>
        <el-button @click="loadData"><el-icon><Search /></el-icon><span>查询</span></el-button>
      </div>

      <el-table v-if="tableData.length" :data="tableData" class="pm-table">
        <el-table-column prop="id" label="编号" width="90" align="center" />
        <el-table-column prop="name" label="项目名称" min-width="260">
          <template #default="{row}">
            <el-link class="project-name-link" type="primary" @click="router.push(`/projects/${row.id}`)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="负责人" min-width="120">
          <template #default="{row}"><span class="manager-name">{{ row.managerName || managerText(row) }}</span></template>
        </el-table-column>
        <el-table-column prop="currentStageName" label="当前阶段" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{row}">
            <el-tag :type="row.status==='active'?'success':'info'" size="small">
              {{ row.status==='active'?'进行中': row.status==='completed'?'已完成':'已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{row}">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="140" fixed="right" align="center">
          <template #default="{row}">
            <div class="table-actions">
              <el-button type="primary" link size="small" @click="router.push(`/projects/${row.id}`)">查看</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)" v-if="auth.user?.role==='admin'">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无项目数据" />

      <div class="pagination-row" v-if="total > 0">
        <el-pagination v-model:current-page="page" :total="total" :page-size="size"
          layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getProjects, deleteProject, importProjectsFromOa } from '../api/project'
import { ElMessage, ElMessageBox } from 'element-plus'
import { confirmDanger, showActionError } from '../utils/actionGuards'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const total = ref(0)
const summaryTotal = ref(null)
const size = ref(10)
const keyword = ref('')
const statusFilter = ref('')
const oaFileInput = ref(null)
const importingOa = ref(false)
const activeCount = ref(null)
const completedCount = ref(null)

async function loadData() {
  loading.value = true
  try {
    const res = await getProjects({ page: page.value, size: size.value, keyword: keyword.value, status: statusFilter.value })
    tableData.value = res.data.records
    total.value = res.data.total
    await loadSummaryCounts()
  } catch (error) {
    showActionError(error, '项目列表加载失败')
  } finally { loading.value = false }
}

async function loadSummaryCounts() {
  try {
    const allRes = await getProjects({ page: 1, size: 999, status: '' })
    const allRecords = allRes?.data?.records || []
    summaryTotal.value = allRes?.data?.total || allRecords.length

    const activeRes = await getProjects({ page: 1, size: 1, status: 'active' })
    activeCount.value = activeRes?.data?.total || 0

    const completedRes = await getProjects({ page: 1, size: 1, status: 'completed' })
    completedCount.value = completedRes?.data?.total || 0
  } catch (e) {
    summaryTotal.value = total.value
  }
}

function formatTime(val) {
  if (!val) return '-'
  return val.substring(0, 16).replace('T', ' ')
}

function setStatusFilter(status) {
  statusFilter.value = status
  page.value = 1
  loadData()
}

async function handleDelete(row) {
  try {
    await confirmDanger(`确定删除项目“${row.name || row.id}”？`, '删除项目')
    await deleteProject(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    showActionError(error, '删除项目失败')
  }
}

function triggerOaImport() {
  oaFileInput.value?.click()
}

function managerText(row) {
  return oaManagerName(row) || '未填写'
}

function managerStatus(row) {
  if (row.managerName) return { text: '已匹配', type: 'success' }
  if (oaManagerName(row)) return { text: '待匹配', type: 'warning' }
  return { text: '未填写', type: 'info' }
}

function oaManagerName(row) {
  const text = row.hrAllocation || ''
  const match = text.match(/项目负责人[:：]\s*([^\n]+)/)
  return match ? match[1] : ''
}

async function handleOaFileSelected(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return

  importingOa.value = true
  try {
    const res = await importProjectsFromOa(file)
    const data = res.data || {}
    const missingManagers = data.missingManagers?.length ? data.missingManagers.join('、') : '无'
    await ElMessageBox.alert(
      `读取项目：${data.totalRows || 0} 条\n新增：${data.createdCount || 0} 条\n更新：${data.updatedCount || 0} 条\n跳过：${data.skippedCount || 0} 条\n负责人已匹配：${data.matchedManagerCount || 0} 条\n未匹配负责人：${missingManagers}`,
      'OA 项目导入结果',
      { confirmButtonText: '知道了' }
    )
    page.value = 1
    await loadData()
  } catch (error) {
    showActionError(error, 'OA 项目导入失败')
  } finally {
    importingOa.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.project-list-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.project-page-heading {
  min-height: 48px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.project-page-heading h2 {
  margin: 0;
  color: var(--pm-text);
  font-size: 22px;
  line-height: 1.4;
  font-weight: 600;
}

.project-page-heading p {
  margin-top: 3px;
  color: var(--pm-text-secondary);
  font-size: 13px;
}

.heading-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.project-summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 12px;
}

.project-summary-item {
  appearance: none;
  min-height: 88px;
  padding: 16px 18px;
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius);
  background: var(--pm-surface);
  display: flex;
  align-items: center;
  gap: 14px;
  text-align: left;
  cursor: pointer;
  transition: border-color var(--pm-duration-fast) var(--pm-ease), box-shadow var(--pm-duration-fast) var(--pm-ease);
}

.project-summary-item:hover,
.project-summary-item.active {
  border-color: var(--pm-accent);
  box-shadow: 0 0 0 1px rgba(22, 119, 255, 0.08);
}

.summary-icon {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 21px;
  flex-shrink: 0;
}

.summary-icon--blue { color: #1677ff; background: #eaf2ff; }
.summary-icon--green { color: #00a870; background: #e8f8f2; }
.summary-icon--gray { color: #646a73; background: #f2f3f5; }

.summary-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.summary-copy strong {
  color: var(--pm-text);
  font-size: 25px;
  line-height: 1.25;
  font-weight: 600;
}

.summary-label {
  color: var(--pm-text-secondary);
  font-size: 13px;
  line-height: 1.4;
}

.project-table-panel {
  margin: 0;
  padding: 16px;
}

.project-search { width: 300px; }
.status-select { width: 150px; }

.pm-table {
  width: 100%;
}

.project-name-link {
  max-width: 100%;
  line-height: 1.5;
  white-space: normal;
  text-align: left;
}

.manager-name {
  color: var(--pm-text);
  font-weight: 500;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

.hidden-file-input {
  display: none;
}

@media (max-width: 900px) {
  .project-page-heading {
    flex-direction: column;
  }

  .project-summary-grid {
    grid-template-columns: 1fr;
  }

  .project-search,
  .status-select {
    width: 100%;
  }
}
</style>
