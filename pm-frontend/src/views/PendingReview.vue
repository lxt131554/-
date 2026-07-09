<template>
  <div class="page-container">
    <div class="page-header"><h2>待审阅填报</h2></div>

    <div class="section-block">
      <!-- Summary cards -->
      <section class="page-summary-grid" style="margin-bottom:16px">
        <div class="summary-card summary-card--primary clickable" :class="{ active: quickFilter==='all' }" @click="quickFilter='all'">
          <div class="summary-card-value">{{ reports.length }}</div>
          <div class="summary-card-label">待审核</div>
          <div class="summary-card-hint">等待审核的填报数量</div>
        </div>
        <div class="summary-card summary-card--danger clickable" :class="{ active: quickFilter==='overdue' }" @click="quickFilter='overdue'">
          <div class="summary-card-value">{{ overdueCount }}</div>
          <div class="summary-card-label">超时未审</div>
          <div class="summary-card-hint">提交超过48小时</div>
        </div>
        <div class="summary-card summary-card--success clickable" :class="{ active: quickFilter==='attachment' }" @click="quickFilter='attachment'">
          <div class="summary-card-value">{{ withAttachmentCount }}</div>
          <div class="summary-card-label">含成果附件</div>
          <div class="summary-card-hint">需要成果审核</div>
        </div>
      </section>

      <!-- Filter bar -->
      <div class="filter-bar">
        <el-select v-model="filterProject" placeholder="按项目筛选" clearable size="small" style="width:220px">
          <el-option v-for="pj in uniqueProjects" :key="pj" :label="pj" :value="pj" />
        </el-select>
        <el-checkbox v-model="filterDeviation" size="small">仅看有偏差</el-checkbox>
        <span v-if="filteredReports.length !== reports.length" style="color:var(--pm-text-muted);font-size:13px">
          筛选结果：{{ filteredReports.length }} / {{ reports.length }} 条
        </span>
      </div>

      <!-- Table with horizontal scroll -->
      <div class="table-scroll-wrapper">
        <el-table
          :data="pagedReports"
          v-loading="loading"
          size="small"
          style="min-width:900px"
        >
          <el-table-column prop="projectName" label="所属项目" min-width="160">
            <template #default="{row}">
              <span class="cell-project-name">{{ row.projectName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="stageName" label="阶段" min-width="120" />
          <el-table-column prop="submitUserName" label="提交人" min-width="100" />
          <el-table-column label="进度" width="85" align="center">
            <template #default="{row}">
              <span class="progress-text">{{ row.progressRate }}%</span>
            </template>
          </el-table-column>
          <el-table-column label="偏差" width="80" align="center">
            <template #default="{row}">
              <el-tag v-if="row.problem && row.problem.trim()" type="danger" size="small">有</el-tag>
              <span v-else class="text-muted">无</span>
            </template>
          </el-table-column>
          <el-table-column label="附件" width="75" align="center">
            <template #default="{row}">
              <el-icon v-if="row.attachmentName" style="color:#059669" :size="16"><Paperclip /></el-icon>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" min-width="140">
            <template #default="{row}">
              <span class="cell-time">{{ formatTime(row.submitTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="210" fixed="right" align="center">
            <template #default="{row}">
              <div class="table-actions">
                <el-button type="primary" link size="small" @click="router.push(`/pending-review/${row.id}`)">
                  查看详情
                </el-button>
                <el-button v-if="row.attachmentName" type="primary" size="small"
                  @click="router.push(`/achievement-review/${row.id}`)">成果审核</el-button>
                <el-button v-else type="primary" size="small"
                  @click="router.push(`/pending-review/${row.id}`)">审阅</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- States -->
      <el-empty v-if="!loading && reports.length===0" description="暂无待审阅填报" :image-size="60" />
      <el-empty v-else-if="!loading && reports.length>0 && filteredReports.length===0" description="未找到匹配的待审阅填报" :image-size="60">
        <el-button size="small" @click="filterProject=''; filterDeviation=false; quickFilter='all'">清除筛选</el-button>
      </el-empty>
      <div v-if="!loading && reports.length>0 && filteredReports.length>0 && filteredReports.length < reports.length" style="text-align:center;padding:4px 0;color:var(--pm-text-muted);font-size:13px">
        已筛选 {{ filteredReports.length }} / {{ reports.length }} 条记录
      </div>
      <el-pagination v-if="filteredReports.length > pageSize"
        v-model:current-page="page" :page-size="pageSize"
        :total="filteredReports.length" layout="prev, pager, next" :pager-count="5" size="small"
        style="margin-top:12px;justify-content:flex-end" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getPendingReviews } from '../api/report'
import { showActionError } from '../utils/actionGuards'

const router = useRouter()
const reports = ref([])
const loading = ref(false)

const filterProject = ref('')
const filterDeviation = ref(false)
const quickFilter = ref('all')

const uniqueProjects = computed(() => [...new Set(reports.value.map(r => r.projectName).filter(Boolean))])

const filteredReports = computed(() => {
  let list = reports.value
  if (filterProject.value) list = list.filter(r => r.projectName === filterProject.value)
  if (filterDeviation.value) list = list.filter(r => r.problem && r.problem.trim())
  if (quickFilter.value === 'overdue') list = list.filter(isOverdue)
  if (quickFilter.value === 'attachment') list = list.filter(r => r.attachmentName)
  return list
})

const overdueCount = computed(() => reports.value.filter(isOverdue).length)
const withAttachmentCount = computed(() => reports.value.filter(r => r.attachmentName).length)

const page = ref(1)
const pageSize = 10
const pagedReports = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredReports.value.slice(start, start + pageSize)
})

watch([filterProject, filterDeviation, quickFilter], () => { page.value = 1 })

function isOverdue(report) {
  if (!report.submitTime) return false
  return (new Date() - new Date(report.submitTime)) / 3600000 > 48
}

function formatTime(val) {
  if (!val) return '-'
  return val.substring(0, 16).replace('T', ' ')
}

async function loadData() {
  loading.value = true
  try {
    const res = await getPendingReviews()
    reports.value = res.data.records || res.data || []
  } catch (error) {
    showActionError(error, '待审阅填报加载失败')
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<style scoped>
.text-muted { color: var(--pm-text-muted); }

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.table-scroll-wrapper {
  overflow-x: auto;
}

.cell-project-name {
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cell-time {
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.progress-text {
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

/* Summary card color accent variants */
.summary-card--primary { border-left: 3px solid var(--pm-accent); }
.summary-card--primary .summary-card-value { color: var(--pm-accent); }
.summary-card--danger { border-left: 3px solid var(--pm-red-text); }
.summary-card--danger .summary-card-value { color: var(--pm-red-text); }
.summary-card--success { border-left: 3px solid var(--pm-green-text); }
.summary-card--success .summary-card-value { color: var(--pm-green-text); }
.summary-card.clickable { cursor: pointer; }
.summary-card.clickable.active {
  border-color: var(--pm-accent);
  box-shadow: inset 0 0 0 1px rgba(37,99,235,0.18), var(--pm-shadow);
}
</style>
