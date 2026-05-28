<template>
  <div class="page-container">
    <div class="page-header">
      <h2>领导看板</h2>
      <p style="color:var(--pm-text-secondary);margin-top:4px">全院项目总览</p>
    </div>

    <div class="stat-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-num" style="color:#409eff">{{ stats.activeProjects ?? '--' }}</div>
        <div class="stat-label">进行中项目</div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-num" style="color:#67c23a">{{ stats.completedProjects ?? '--' }}</div>
        <div class="stat-label">已完成项目</div>
      </el-card>
      <el-card class="stat-card" shadow="hover" @click="$router.push('/deviations')">
        <div class="stat-num" style="color:#e6a23c">{{ stats.openDeviations ?? '--' }}</div>
        <div class="stat-label">未关闭偏差</div>
      </el-card>
      <el-card class="stat-card" shadow="hover" @click="$router.push('/supports')">
        <div class="stat-num" style="color:#f56c6c">{{ stats.pendingSupports ?? '--' }}</div>
        <div class="stat-label">待处理支持</div>
      </el-card>
    </div>

    <div class="card-box" style="margin-top:16px">
      <el-table :data="projects" border stripe v-loading="loading" empty-text="暂无项目">
        <el-table-column prop="name" label="项目名称" min-width="200">
          <template #default="{row}">
            <el-link type="primary" @click="$router.push(`/projects/${row.id}`)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100" align="center">
          <template #default="{row}">
            <el-tag :type="row.status=='active'?'':'success'" size="small">{{ row.status=='active'?'进行中':'已完成' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" min-width="100" align="center">
          <template #default="{row}">
            <el-button text type="primary" size="small" @click="$router.push(`/projects/${row.id}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/index'

const stats = ref({})
const projects = ref([])
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/leader-dashboard')
    stats.value = res.data
    projects.value = res.data.projects || []
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<style scoped>
.stat-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; margin-bottom: 8px; }
.stat-card { cursor: default; text-align: center; padding: 20px 0; }
.stat-card:has(.cursor-pointer) { cursor: pointer; }
.stat-num { font-size: 52px; font-weight: 700; line-height: 1.2; font-variant-numeric: tabular-nums; }
.stat-label { font-size: 15px; color: var(--pm-text-secondary); margin-top: 6px; letter-spacing: 0.5px; }
</style>
