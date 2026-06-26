<template>
  <div class="page-container">
    <div class="page-header">
      <h2>经验库</h2>
      <p style="color:var(--pm-text-secondary);margin-top:4px">沉淀已完成项目的复盘经验、短板和改进建议</p>
    </div>

    <section class="page-summary-grid" style="margin-bottom:16px">
      <div class="summary-card summary-card--primary">
        <div class="summary-card-value">{{ tableData.length }}</div>
        <div class="summary-card-label">经验记录</div>
        <div class="summary-card-hint">已归档的项目经验</div>
      </div>
      <div class="summary-card summary-card--success">
        <div class="summary-card-value">{{ withReusableCount }}</div>
        <div class="summary-card-label">可复用经验</div>
        <div class="summary-card-hint">包含可复用方法</div>
      </div>
      <div class="summary-card summary-card--warning">
        <div class="summary-card-value">{{ withImprovementCount }}</div>
        <div class="summary-card-label">改进建议</div>
        <div class="summary-card-hint">可用于后续项目避坑</div>
      </div>
    </section>

    <div class="section-block">
      <div class="filter-bar">
        <el-input v-model="keyword" clearable placeholder="搜索项目、经验、短板或改进建议" style="max-width:360px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <el-table v-if="filteredData.length" :data="filteredData" border stripe v-loading="loading">
        <el-table-column label="项目名称" min-width="160">
          <template #default="{row}">
            <el-link v-if="row.projectId" type="primary" @click="router.push(`/projects/${row.projectId}`)">{{ row.projectName && row.projectName !== '-' ? row.projectName : (row.projectId || '-') }}</el-link>
            <span v-else>{{ row.projectName && row.projectName !== '-' ? row.projectName : (row.projectId || '-') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="可复用经验" min-width="200">
          <template #default="{row}">
            <div class="line-clamp-2">{{ row.reusableExperience || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="短板或缺陷" min-width="180">
          <template #default="{row}">
            <div class="line-clamp-2">{{ row.shortcomings || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="改进建议" min-width="180">
          <template #default="{row}">
            <div class="line-clamp-2">{{ row.improvement || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="150">
          <template #default="{row}">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{row}">
            <el-button v-if="row.projectId" text type="primary" size="small" @click="router.push(`/projects/${row.projectId}`)">查看详情</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无经验总结" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/index'
import { showActionError } from '../utils/actionGuards'

const router = useRouter()
const tableData = ref([])
const loading = ref(false)
const keyword = ref('')

const filteredData = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return tableData.value
  return tableData.value.filter(item => {
    return [
      item.projectName,
      item.reusableExperience,
      item.shortcomings,
      item.risks,
      item.improvement
    ].some(value => String(value || '').toLowerCase().includes(text))
  })
})

const withReusableCount = computed(() => tableData.value.filter(item => item.reusableExperience).length)
const withImprovementCount = computed(() => tableData.value.filter(item => item.improvement).length)

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/experiences')
    tableData.value = res.data || []
  } catch (error) {
    showActionError(error, '经验库加载失败')
  } finally { loading.value = false }
}

function formatTime(val) {
  if (!val) return '-'
  return val.substring(0, 16).replace('T', ' ')
}

onMounted(loadData)
</script>

<style scoped>
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
.summary-card--warning {
  border-left: 3px solid var(--pm-amber-text);
}
.summary-card--warning .summary-card-value {
  color: var(--pm-amber-text);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
