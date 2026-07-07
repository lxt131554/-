<template>
  <div class="page-container">
    <div class="page-header"><h2>我的待填报</h2></div>

    <!-- Summary cards -->
    <section class="page-summary-grid">
      <div class="summary-card summary-card--primary clickable" @click="$router.push('/my-tasks')">
        <div class="summary-card-value">{{ tasks.length }}</div>
        <div class="summary-card-label">全部任务</div>
        <div class="summary-card-hint">当前分配给我的阶段</div>
      </div>
      <div class="summary-card summary-card--danger">
        <div class="summary-card-value">{{ pendingCount }}</div>
        <div class="summary-card-label">待填报</div>
        <div class="summary-card-hint">需要提交进度汇报</div>
      </div>
      <div class="summary-card summary-card--warning">
        <div class="summary-card-value">{{ inProgressCount }}</div>
        <div class="summary-card-label">进行中/待重新填报</div>
        <div class="summary-card-hint">被退回需重新提交</div>
      </div>
      <div class="summary-card summary-card--success">
        <div class="summary-card-value">{{ completedCount }}</div>
        <div class="summary-card-label">已完成</div>
        <div class="summary-card-hint">已通过审核</div>
      </div>
    </section>

    <div class="section-block">
      <!-- Filter bar -->
      <div class="filter-bar">
        <el-select v-model="filterProject" placeholder="按项目筛选" clearable size="small" style="width:200px">
          <el-option v-for="pj in uniqueProjects" :key="pj" :label="pj" :value="pj" />
        </el-select>
        <el-radio-group v-model="filterStatus" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="pending">待填报</el-radio-button>
          <el-radio-button value="in_progress">待重新填报</el-radio-button>
          <el-radio-button value="submitted">待审阅</el-radio-button>
          <el-radio-button value="completed">已完成</el-radio-button>
        </el-radio-group>
      </div>

      <div style="overflow-x:auto">
        <el-table :data="filteredTasks" v-loading="loading">
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="stageName" label="阶段名称" min-width="140">
          <template #default="{row}">
            <el-link type="primary" @click="router.push(`/stages/${row.id}`)">{{ row.stageName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="阶段描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="计划时间" min-width="200">
          <template #default="{row}">{{ row.planStart || '-' }} 至 {{ row.planEnd || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{row}">
            <el-tag v-if="row.status==='pending'" type="info" size="small">待填报</el-tag>
            <el-tag v-else-if="row.status==='in_progress'" type="warning" size="small">待重新填报</el-tag>
            <el-tag v-else-if="row.status==='submitted'" type="primary" size="small">待审阅</el-tag>
            <el-tag v-else-if="row.status==='completed'" type="success" size="small">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{row}">
            <el-button v-if="(row.status==='pending' || row.status==='in_progress') && row.projectStatus !== 'completed'" type="primary" size="small" @click="router.push(`/my-tasks/${row.id}/report`)">填报</el-button>
            <el-tooltip v-else-if="(row.status==='pending' || row.status==='in_progress') && row.projectStatus === 'completed'" content="项目已完成，无法提交" placement="top">
              <el-button type="primary" size="small" disabled>填报</el-button>
            </el-tooltip>
            <el-button v-else-if="row.status==='submitted'" type="primary" size="small" link @click="router.push(`/my-tasks/${row.id}/report`)">查看填报</el-button>
            <el-tooltip v-else-if="row.status==='completed'" content="阶段已完成" placement="top">
              <el-button type="primary" size="small" link disabled>已完成</el-button>
            </el-tooltip>
            <el-button v-else type="primary" size="small" link @click="router.push(`/stages/${row.id}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <el-empty v-if="!loading && tasks.length===0" description="暂无需填报的阶段" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyTasks } from '../api/stage'

const router = useRouter()
const tasks = ref([])
const loading = ref(false)
const filterProject = ref('')
const filterStatus = ref('')

const pendingCount = computed(() => tasks.value.filter(t => t.status === 'pending').length)
const inProgressCount = computed(() => tasks.value.filter(t => t.status === 'in_progress').length)
const completedCount = computed(() => tasks.value.filter(t => t.status === 'completed').length)

const uniqueProjects = computed(() => [...new Set(tasks.value.map(t => t.projectName).filter(Boolean))])

const filteredTasks = computed(() => {
  let list = tasks.value
  if (filterProject.value) list = list.filter(t => t.projectName === filterProject.value)
  if (filterStatus.value) list = list.filter(t => t.status === filterStatus.value)
  return list
})

async function loadTasks() {
  loading.value = true
  try {
    const res = await getMyTasks()
    tasks.value = res.data
  } finally { loading.value = false }
}

onMounted(loadTasks)
</script>

<style scoped>
.summary-card.clickable { cursor: pointer; }

.summary-card--primary { border-left: 3px solid var(--pm-accent); }
.summary-card--primary .summary-card-value { color: var(--pm-accent); }
.summary-card--warning { border-left: 3px solid #F59E0B; }
.summary-card--warning .summary-card-value { color: #F59E0B; }
.summary-card--danger { border-left: 3px solid var(--pm-red-text); }
.summary-card--danger .summary-card-value { color: var(--pm-red-text); }
.summary-card--success { border-left: 3px solid var(--pm-green-text); }
.summary-card--success .summary-card-value { color: var(--pm-green-text); }

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
</style>
