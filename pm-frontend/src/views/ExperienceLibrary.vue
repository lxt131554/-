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

      <!-- Experience cards -->
      <div v-if="pagedData.length" v-loading="loading">
        <div v-for="item in pagedData" :key="item.id"
          class="exp-card" @click="router.push(`/projects/${item.projectId}`)" style="cursor:pointer">
          <div class="exp-card-header">
            <el-link type="primary" :underline="false" style="font-size:15px;font-weight:600">
              {{ item.projectName || '项目#' + item.projectId }}
            </el-link>
            <span style="color:var(--pm-text-muted);font-size:13px">{{ formatTime(item.createTime) }}</span>
          </div>
          <div v-if="item.reusableExperience" class="exp-card-section">
            <div class="exp-card-label">可复用经验</div>
            <div class="exp-card-text">{{ item.reusableExperience }}</div>
          </div>
          <div v-if="item.shortcomings" class="exp-card-section">
            <div class="exp-card-label">短板或缺陷</div>
            <div class="exp-card-text">{{ item.shortcomings }}</div>
          </div>
          <div v-if="item.improvement" class="exp-card-section">
            <div class="exp-card-label">改进建议</div>
            <div class="exp-card-text">{{ item.improvement }}</div>
          </div>
        </div>

        <el-pagination v-if="filteredData.length > expPageSize"
          v-model:current-page="expPage" :page-size="expPageSize"
          :total="filteredData.length" layout="prev, pager, next" :pager-count="5" size="small"
          style="margin-top:16px;justify-content:flex-end" />
      </div>
      <el-empty v-else-if="!loading" description="暂无经验总结" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/index'
import { showActionError } from '../utils/actionGuards'

const router = useRouter()
const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const expPage = ref(1)
const expPageSize = 10

const pagedData = computed(() => {
  const start = (expPage.value - 1) * expPageSize
  return filteredData.value.slice(start, start + expPageSize)
})

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

watch(keyword, () => { expPage.value = 1 })
onMounted(loadData)
</script>

<style scoped>
.summary-card--primary { border-left: 3px solid var(--pm-accent); }
.summary-card--primary .summary-card-value { color: var(--pm-accent); }
.summary-card--success { border-left: 3px solid var(--pm-green-text); }
.summary-card--success .summary-card-value { color: var(--pm-green-text); }
.summary-card--warning { border-left: 3px solid var(--pm-amber-text); }
.summary-card--warning .summary-card-value { color: var(--pm-amber-text); }

.exp-card {
  padding: 20px 0;
  border-bottom: 1px solid var(--pm-border);
  transition: background 0.15s;
}
.exp-card:last-child { border-bottom: none; }
.exp-card:hover { background: var(--pm-surface-hover); }
.exp-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.exp-card-section {
  margin-bottom: 10px;
}
.exp-card-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-secondary);
  margin-bottom: 4px;
}
.exp-card-text {
  font-size: 14px;
  line-height: 1.7;
  color: var(--pm-text);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
