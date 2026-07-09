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
      <!-- Search Toolbar -->
      <div class="exp-toolbar">
        <el-input v-model="keyword" clearable placeholder="搜索项目名称、经验、短板或改进建议" style="width:380px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <span class="exp-result-count">共 {{ filteredData.length }} 条经验</span>
      </div>

      <!-- Experience cards -->
      <div v-if="filteredData.length" v-loading="loading">
        <div v-for="item in filteredData" :key="item.id" class="exp-card">
          <div class="exp-card-header">
            <div>
              <span class="exp-title">{{ item.projectName || '项目#' + item.projectId }}</span>
              <div class="exp-tags">
                <el-tag size="small" v-if="item.reusableExperience">可复用经验</el-tag>
                <el-tag size="small" type="warning" v-if="item.shortcomings">短板或缺陷</el-tag>
                <el-tag size="small" type="info" v-if="item.improvement">改进建议</el-tag>
              </div>
            </div>
            <span class="exp-time">{{ formatTime(item.createTime) }}</span>
          </div>

          <div class="exp-summary-grid">
            <div class="exp-summary-item">
              <div class="exp-summary-label">可复用经验</div>
              <div class="exp-summary-text line-clamp-2">{{ item.reusableExperience || '暂无内容' }}</div>
            </div>
            <div class="exp-summary-item">
              <div class="exp-summary-label">短板或缺陷</div>
              <div class="exp-summary-text line-clamp-2">{{ item.shortcomings || '暂无内容' }}</div>
            </div>
            <div class="exp-summary-item">
              <div class="exp-summary-label">改进建议</div>
              <div class="exp-summary-text line-clamp-2">{{ item.improvement || '暂无内容' }}</div>
            </div>
          </div>

          <div class="exp-card-footer">
            <el-button type="primary" link size="small" @click="openDetail(item)">查看详情</el-button>
          </div>
        </div>

        <el-pagination v-if="total > expPageSize"
          v-model:current-page="expPage" :page-size="expPageSize"
          :total="total" layout="prev, pager, next" :pager-count="5" size="small"
          @current-change="loadData"
          style="margin-top:16px;justify-content:flex-end" />
      </div>
      <el-empty v-else-if="!loading" description="暂无经验总结" />
    </div>

    <!-- Detail Drawer -->
    <el-drawer v-model="showDetail" title="经验详情" size="720px" :lock-scroll="true" append-to-body>
      <template v-if="detailItem">
        <div class="drawer-meta">
          <span class="drawer-project">{{ detailItem.projectName || '项目#'+detailItem.projectId }}</span>
          <span class="drawer-time">{{ formatTime(detailItem.createTime) }}</span>
        </div>
        <div v-if="detailItem.reusableExperience" class="drawer-section">
          <div class="drawer-label">可复用经验</div>
          <div class="drawer-text long-text">{{ detailItem.reusableExperience }}</div>
        </div>
        <div v-if="detailItem.shortcomings" class="drawer-section">
          <div class="drawer-label">短板或缺陷</div>
          <div class="drawer-text long-text">{{ detailItem.shortcomings }}</div>
        </div>
        <div v-if="detailItem.improvement" class="drawer-section">
          <div class="drawer-label">改进建议</div>
          <div class="drawer-text long-text">{{ detailItem.improvement }}</div>
        </div>
      </template>
    </el-drawer>
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
const total = ref(0)
const showDetail = ref(false)
const detailItem = ref(null)

function openDetail(item) { detailItem.value = item; showDetail.value = true }
function goProject(pid) { showDetail.value = false; router.push('/projects/' + pid) }

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
    const params = { page: expPage.value, size: expPageSize }
    const res = await request.get('/experiences', { params })
    tableData.value = (res.data && res.data.records) || res.data || []
    total.value = res.data.total || res.data.length || 0
  } catch (error) {
    showActionError(error, '经验库加载失败')
  } finally { loading.value = false }
}

function formatTime(val) {
  if (!val) return '-'
  return val.substring(0, 16).replace('T', ' ')
}

watch(keyword, () => { expPage.value = 1; loadData() })
onMounted(loadData)
</script>

<style scoped>
/* ---- Stats cards (fine-tuned) ---- */
.summary-card--primary { border-left: 3px solid var(--pm-accent); }
.summary-card--primary .summary-card-value { color: var(--pm-accent); }
.summary-card--success { border-left: 3px solid var(--pm-green-text); }
.summary-card--success .summary-card-value { color: var(--pm-green-text); }
.summary-card--warning { border-left: 3px solid var(--pm-amber-text); }
.summary-card--warning .summary-card-value { color: var(--pm-amber-text); }

/* ---- Toolbar ---- */
.exp-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.exp-result-count {
  font-size: 13px;
  color: var(--pm-text-muted);
  white-space: nowrap;
}

/* ---- Cards ---- */
.exp-card {
  background: var(--pm-surface);
  border: 1px solid var(--pm-border);
  border-radius: 8px;
  padding: 20px 24px;
  margin-bottom: 12px;
}
.exp-card:hover {
  border-color: var(--pm-accent);
}

.exp-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}
.exp-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--pm-text);
}
.exp-tags {
  display: flex;
  gap: 6px;
  margin-top: 6px;
}
.exp-time {
  font-size: 13px;
  color: var(--pm-text-muted);
  white-space: nowrap;
}

.exp-summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 14px;
}
.exp-summary-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-secondary);
  margin-bottom: 6px;
}
.exp-summary-text {
  font-size: 14px;
  color: var(--pm-text);
  line-height: 1.6;
}
.exp-summary-text:empty::after {
  content: '暂无内容';
  color: var(--pm-text-muted);
}

.exp-card-footer {
  display: flex;
  justify-content: flex-end;
}

/* ---- Drawer ---- */
.drawer-meta {
  margin-bottom: 20px;
}
.drawer-project {
  font-size: 16px;
  font-weight: 600;
}
.drawer-time {
  font-size: 13px;
  color: var(--pm-text-muted);
  margin-left: 12px;
}
.drawer-section {
  margin-bottom: 24px;
}
.drawer-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--pm-text);
  margin-bottom: 8px;
  padding-left: 10px;
  border-left: 3px solid var(--pm-accent);
}
.drawer-text {
  background: var(--pm-bg);
  border-radius: 6px;
  padding: 14px 16px;
  line-height: 1.8;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  word-break: break-word;
}

/* ---- Utilities ---- */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.long-text {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  word-break: break-word;
  line-height: 1.75;
}

/* ---- Responsive ---- */
@media (max-width: 900px) {
  .exp-summary-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 600px) {
  .exp-card {
    padding: 16px;
  }
  .exp-card-header {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
