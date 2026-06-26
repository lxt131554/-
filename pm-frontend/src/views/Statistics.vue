<template>
  <div class="page-container">
    <div class="page-header">
      <h2>统计分析</h2>
      <p style="color:var(--pm-text-secondary);margin-top:4px">全院项目数据概览</p>
    </div>

    <!-- Summary cards -->
    <div class="stat-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-num" style="color:#409eff">{{ stats.totalProjects ?? '--' }}</div>
        <div class="stat-label">项目总数</div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-num" style="color:#67c23a">{{ stats.activeProjects ?? '--' }}</div>
        <div class="stat-label">进行中</div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-num" style="color:#909399">{{ stats.completedProjects ?? '--' }}</div>
        <div class="stat-label">已完成</div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-num" style="color:#e6a23c">{{ stats.overdueStages ?? '--' }}</div>
        <div class="stat-label">逾期阶段</div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-num" style="color:#f56c6c">{{ stats.openDeviations ?? '--' }}</div>
        <div class="stat-label">未关闭偏差</div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-num" style="color:#e6a23c">{{ stats.pendingSupports ?? '--' }}</div>
        <div class="stat-label">待处理支持</div>
      </el-card>
    </div>

    <!-- Department stats -->
    <div class="card-box" style="margin-top:16px">
      <div class="page-toolbar">
        <span style="font-weight:600;font-size:16px">按部门统计</span>
      </div>
      <el-table v-if="(stats.deptStats || []).length" :data="stats.deptStats" border stripe>
        <el-table-column prop="dept" label="部门" min-width="160" />
        <el-table-column prop="projects" label="项目数" min-width="120" align="center" sortable />
        <el-table-column prop="experiences" label="经验总结数" min-width="120" align="center" sortable />
      </el-table>
      <el-empty v-else description="暂无部门统计数据" />
    </div>

    <!-- Monthly stats -->
    <div class="card-box" style="margin-top:16px">
      <div class="page-toolbar">
        <span style="font-weight:600;font-size:16px">按月统计（新项目数量）</span>
      </div>
      <el-table v-if="(stats.monthlyStats || []).length" :data="stats.monthlyStats" border stripe max-height="400">
        <el-table-column prop="month" label="月份" min-width="160" />
        <el-table-column prop="count" label="新建项目数" min-width="140" align="center" sortable>
          <template #default="{row}">
            <el-tag type="primary" size="small">{{ row.count }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无月度统计数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/index'
import { showActionError } from '../utils/actionGuards'

const stats = ref({})

async function loadStats() {
  try {
    const res = await request.get('/statistics')
    stats.value = res.data
  } catch (error) {
    showActionError(error, '统计数据加载失败')
  }
}

onMounted(loadStats)
</script>

<style scoped>
.stat-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 16px; margin-bottom: 8px; }
.stat-card { text-align: center; padding: 20px 0; }
.stat-num { font-size: 40px; font-weight: 700; line-height: 1.2; }
.stat-label { font-size: 13px; color: var(--pm-text-secondary); margin-top: 6px; letter-spacing: 0.5px; }
</style>
