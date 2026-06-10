<template>
  <div class="page-container">
    <div class="page-header"><h2>支持事项</h2></div>
    <section class="page-summary-grid" style="margin-bottom:16px">
      <div class="summary-card summary-card--warning">
        <div class="summary-card-value">{{ pendingCount }}</div>
        <div class="summary-card-label">待处理</div>
        <div class="summary-card-hint">需要协调解决</div>
      </div>
      <div class="summary-card summary-card--success">
        <div class="summary-card-value">{{ resolvedCount }}</div>
        <div class="summary-card-label">已解决</div>
        <div class="summary-card-hint">已处理完成</div>
      </div>
      <div class="summary-card summary-card--primary">
        <div class="summary-card-value">{{ tableData.length }}</div>
        <div class="summary-card-label">全部事项</div>
      </div>
    </section>
    <div class="section-block">
      <div class="filter-bar">
        <el-radio-group v-model="filterStatus" @change="loadData" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="pending">待处理</el-radio-button>
          <el-radio-button value="resolved">已解决</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="$router.push('/supports/new')">
          <el-icon><Plus /></el-icon> 新建支持申请
        </el-button>
      </div>
      <el-table v-if="tableData.length" :data="tableData" v-loading="loading">
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="title" label="事项标题" min-width="220" show-overflow-tooltip>
          <template #default="{row}">
            <el-link type="primary" @click="router.push(`/supports/${row.id}`)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" min-width="100" />
        <el-table-column prop="handlerName" label="处理人" min-width="100" />
        <el-table-column prop="expectTime" label="期望时间" min-width="120" />
        <el-table-column prop="status" label="状态" min-width="90" align="center">
          <template #default="{row}">
            <el-tag :type="row.status=='pending'?'warning':'success'" size="small">
              {{ row.status=='pending'?'待处理':'已解决' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="80" fixed="right" align="center">
          <template #default="{row}">
            <el-button v-if="row.status=='pending'" text type="primary" size="small"
              @click="$router.push(`/supports/${row.id}`)">处理</el-button>
            <el-button v-else text size="small" @click="$router.push(`/supports/${row.id}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无支持事项" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/index'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const filterStatus = ref('')

const pendingCount = computed(() => tableData.value.filter(s => s.status === 'pending').length)
const resolvedCount = computed(() => tableData.value.filter(s => s.status === 'resolved').length)

async function loadData() {
  loading.value = true
  try {
    const params = filterStatus.value ? { status: filterStatus.value } : {}
    const res = await request.get('/supports', { params })
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(loadData)
</script>
