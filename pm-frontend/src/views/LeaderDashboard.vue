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

    <!-- 逾期项目警告 -->
    <el-card v-if="stats.openDeviations > 0" class="warning-card" shadow="hover" @click="$router.push('/deviations')" style="margin-bottom:16px">
      <div style="display:flex;align-items:center;justify-content:space-between">
        <span style="color:var(--pm-text)">有 {{ stats.openDeviations }} 项未关闭偏差需要关注</span>
        <el-button text type="primary">查看</el-button>
      </div>
    </el-card>

    <!-- 待处理偏差 -->
    <div class="card-box" style="margin-top:16px">
      <div class="page-toolbar">
        <span style="font-weight:600;font-size:16px">待处理偏差</span>
        <el-button v-if="openDeviationList.length" text type="primary" @click="$router.push('/deviations')">查看全部</el-button>
      </div>
      <el-table v-if="openDeviationList.length" :data="openDeviationList" border stripe>
        <el-table-column prop="projectName" label="项目" min-width="160" />
        <el-table-column prop="description" label="偏差描述" min-width="240" show-overflow-tooltip>
          <template #default="{row}">
            <el-link type="primary" @click="$router.push(`/deviations/${row.id}`)">{{ row.description }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="createUserName" label="创建人" min-width="100" />
        <el-table-column prop="createTime" label="时间" min-width="160" />
      </el-table>
      <el-empty v-else description="暂无待处理偏差" :image-size="80" />
    </div>

    <!-- 待处理支持事项 -->
    <div class="card-box" style="margin-top:16px">
      <div class="page-toolbar">
        <span style="font-weight:600;font-size:16px">待处理支持事项</span>
        <el-button v-if="pendingSupportList.length" text type="primary" @click="$router.push('/supports')">查看全部</el-button>
      </div>
      <el-table v-if="pendingSupportList.length" :data="pendingSupportList" border stripe>
        <el-table-column prop="projectName" label="项目" min-width="160" />
        <el-table-column prop="title" label="事项" min-width="240" show-overflow-tooltip>
          <template #default="{row}">
            <el-link type="primary" @click="$router.push(`/supports/${row.id}`)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" min-width="100" />
        <el-table-column prop="createTime" label="时间" min-width="160" />
      </el-table>
      <el-empty v-else description="暂无待处理支持事项" :image-size="80" />
    </div>

    <!-- 待确认变更 -->
    <div class="card-box" style="margin-top:16px">
      <div class="page-toolbar">
        <span style="font-weight:600;font-size:16px">待确认变更</span>
        <el-button v-if="pendingChangeList.length" text type="primary" @click="$router.push('/projects')">查看全部</el-button>
      </div>
      <el-table v-if="pendingChangeList.length" :data="pendingChangeList" border stripe>
        <el-table-column prop="projectName" label="项目" min-width="160" />
        <el-table-column prop="content" label="变更内容" min-width="240" show-overflow-tooltip>
          <template #default="{row}">
            <el-link type="primary" @click="$router.push(`/changes/${row.id}`)">{{ row.content }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="confirmTime" label="确认时间" min-width="120" />
        <el-table-column label="操作" min-width="100" align="center">
          <template #default="{row}">
            <el-button text type="success" size="small" @click="$router.push(`/projects/${row.projectId}`)">去处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无待确认变更" :image-size="80" />
    </div>

    <!-- 项目列表 -->
    <div class="card-box" style="margin-top:16px">
      <div class="page-toolbar">
        <span style="font-weight:600;font-size:16px">全院项目</span>
      </div>
      <el-table v-if="projects.length" :data="projects" border stripe v-loading="loading">
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
      <el-empty v-else-if="!loading" description="暂无项目" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../api/index'

const stats = ref({})
const projects = ref([])
const loading = ref(false)

const openDeviationList = computed(() => stats.value.openDeviationList || [])
const pendingSupportList = computed(() => stats.value.pendingSupportList || [])
const pendingChangeList = computed(() => stats.value.pendingChanges || [])

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
.warning-card { border: 1px solid rgba(245,108,108,0.2); }
</style>
