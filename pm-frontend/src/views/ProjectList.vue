<template>
  <div class="page-container">
    <div class="page-header">
      <h2>项目列表</h2>
    </div>
    <section class="page-summary-grid" style="margin-bottom:16px">
      <div class="summary-card summary-card--primary clickable" :class="{ active: statusFilter==='' }" @click="setStatusFilter('')">
        <div class="summary-card-value">{{ summaryTotal ?? '--' }}</div>
        <div class="summary-card-label">全部项目</div>
      </div>
      <div class="summary-card summary-card--success clickable" :class="{ active: statusFilter==='active' }" @click="setStatusFilter('active')">
        <div class="summary-card-value">{{ activeCount ?? '--' }}</div>
        <div class="summary-card-label">进行中</div>
      </div>
      <div class="summary-card summary-card--primary clickable" :class="{ active: statusFilter==='completed' }" @click="setStatusFilter('completed')">
        <div class="summary-card-value">{{ completedCount ?? '--' }}</div>
        <div class="summary-card-label">已完成</div>
      </div>
    </section>
    <div class="section-block">
      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="项目状态" clearable style="width:140px;margin-right:8px" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="进行中" value="active" />
          <el-option label="已完成" value="completed" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索项目名称" clearable style="width:280px" @clear="loadData" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div>
          <input ref="oaFileInput" class="hidden-file-input" type="file" accept=".xls,.xlsx" @change="handleOaFileSelected" />
          <el-button :loading="importingOa" @click="triggerOaImport" v-if="auth.user?.role==='admin'">
            <el-icon><Upload /></el-icon> 导入 OA 项目
          </el-button>
          <el-button type="primary" @click="router.push('/projects/create')" v-if="auth.user?.role==='manager'||auth.user?.role==='admin'">
            <el-icon><Plus /></el-icon> 新建项目
          </el-button>
        </div>
      </div>

      <el-table v-if="tableData.length" :data="tableData" v-loading="loading" class="pm-table">
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="name" label="项目名称" min-width="200">
          <template #default="{row}">
            <el-link type="primary" @click="router.push(`/projects/${row.id}`)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="负责人" min-width="120">
          <template #default="{row}">{{ row.managerName || managerText(row) }}</template>
        </el-table-column>
        <el-table-column prop="currentStageName" label="当前阶段" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{row}">
            <el-tag :type="row.status==='active'?'success':'info'" size="small">
              {{ row.status==='active'?'进行中': row.status==='completed'?'已完成':'已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="150">
          <template #default="{row}">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{row}">
            <el-button text type="primary" @click="router.push(`/projects/${row.id}`)">查看</el-button>
            <el-button text type="danger" @click="handleDelete(row)" v-if="auth.user?.role==='admin'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无项目数据" />

      <el-pagination v-if="total > 0" v-model:current-page="page" :total="total" :page-size="size"
        layout="total, prev, pager, next" @current-change="loadData" style="margin-top:16px;justify-content:flex-end" />
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getProjects, deleteProject, importProjectsFromOa } from '../api/project'
import { ElMessage } from 'element-plus'
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
.pm-table {
  width: 100%;
}

.hidden-file-input {
  display: none;
}

.summary-card--primary {
  border-left: 3px solid var(--pm-accent);
}
.summary-card--primary .summary-card-value {
  color: var(--pm-accent);
}

.summary-card--success {
  border-left: 3px solid var(--pm-green-text);
}
.summary-card--success .summary-card-value {
  color: var(--pm-green-text);
}

.summary-card.clickable.active {
  border-color: var(--pm-accent);
  box-shadow: inset 0 0 0 1px rgba(37,99,235,0.18), var(--pm-shadow);
}
</style>
