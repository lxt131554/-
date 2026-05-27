<template>
  <div class="page-container">
    <div class="page-header"><h2>支持事项</h2></div>
    <div class="card-box">
      <div class="page-toolbar">
        <el-radio-group v-model="filterStatus" @change="loadData" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="pending">待处理</el-radio-button>
          <el-radio-button value="resolved">已解决</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="$router.push('/supports/new')">
          <el-icon><Plus /></el-icon> 新建支持申请
        </el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无支持事项">
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="title" label="事项标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="handlerName" label="处理人" width="100" />
        <el-table-column prop="expectTime" label="期望时间" width="120" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{row}">
            <el-tag :type="row.status=='pending'?'warning':'success'" size="small">
              {{ row.status=='pending'?'待处理':'已解决' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{row}">
            <el-button v-if="row.status=='pending'" text type="primary" size="small"
              @click="$router.push(`/supports/${row.id}`)">处理</el-button>
            <el-button v-else text size="small" @click="$router.push(`/supports/${row.id}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/index'

const loading = ref(false)
const tableData = ref([])
const filterStatus = ref('')

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
