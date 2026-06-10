<template>
  <div class="page-container">
    <div class="page-header"><h2>待审阅填报</h2></div>
    <div class="card-box">

      <!-- Summary cards -->
      <section class="page-summary-grid" style="margin-bottom:16px">
        <div class="summary-card summary-card--primary">
          <div class="summary-card-value">{{ reports.length }}</div>
          <div class="summary-card-label">待审核</div>
          <div class="summary-card-hint">等待审核的填报数量</div>
        </div>
        <div class="summary-card summary-card--danger">
          <div class="summary-card-value">{{ overdueCount }}</div>
          <div class="summary-card-label">超时未审</div>
          <div class="summary-card-hint">提交超过48小时</div>
        </div>
        <div class="summary-card summary-card--success">
          <div class="summary-card-value">{{ withAttachmentCount }}</div>
          <div class="summary-card-label">含成果附件</div>
          <div class="summary-card-hint">需要成果审核</div>
        </div>
      </section>

      <!-- Filter bar -->
      <div class="filter-bar">
        <el-select v-model="filterProject" placeholder="按项目筛选" clearable size="small" style="width:200px">
          <el-option v-for="pj in uniqueProjects" :key="pj" :label="pj" :value="pj" />
        </el-select>
        <el-checkbox v-model="filterDeviation" size="small">仅看有偏差</el-checkbox>
      </div>

      <!-- Table -->
      <el-table :data="filteredReports" v-loading="loading">
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="stageName" label="阶段" min-width="140" />
        <el-table-column prop="submitUserName" label="提交人" min-width="100" />
        <el-table-column prop="content" label="填报内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="problem" label="存在问题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="progressRate" label="进度" min-width="80">
          <template #default="{row}">{{ row.progressRate }}%</template>
        </el-table-column>
        <el-table-column label="偏差" width="80" align="center">
          <template #default="{row}">
            <el-tag v-if="row.problem && row.problem.trim()" type="danger" size="small">有</el-tag>
            <span v-else style="color:var(--pm-text-muted)">—</span>
          </template>
        </el-table-column>
        <el-table-column label="附件" min-width="80" align="center">
          <template #default="{row}">
            <el-icon v-if="row.attachmentName" style="color:#059669"><Paperclip /></el-icon>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" min-width="170" />
        <el-table-column label="操作" min-width="130">
          <template #default="{row}">
            <el-button v-if="row.attachmentName" type="success" size="small"
              @click="router.push(`/achievement-review/${row.id}`)">成果审核</el-button>
            <el-button v-else type="primary" size="small"
              @click="router.push(`/pending-review/${row.id}`)">审阅</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && reports.length===0" description="暂无待审阅填报" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPendingReviews } from '../api/report'

const router = useRouter()
const reports = ref([])
const loading = ref(false)

const filterProject = ref('')
const filterDeviation = ref(false)

const uniqueProjects = computed(() => [...new Set(reports.value.map(r => r.projectName).filter(Boolean))])

const filteredReports = computed(() => {
  let list = reports.value
  if (filterProject.value) list = list.filter(r => r.projectName === filterProject.value)
  if (filterDeviation.value) list = list.filter(r => r.problem && r.problem.trim())
  return list
})

const overdueCount = computed(() => {
  const now = new Date()
  return reports.value.filter(r => {
    if (!r.submitTime) return false
    return (now - new Date(r.submitTime)) / 3600000 > 48
  }).length
})

const withAttachmentCount = computed(() => {
  return reports.value.filter(r => r.attachmentName).length
})

async function loadData() {
  loading.value = true
  try {
    const res = await getPendingReviews()
    reports.value = res.data
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

/* Summary card color accent variants */
.summary-card--primary {
  border-left: 3px solid var(--pm-accent);
}
.summary-card--primary .summary-card-value {
  color: var(--pm-accent);
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
