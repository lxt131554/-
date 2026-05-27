<template>
  <div class="page-container">
    <div class="page-header"><h2>待审阅填报</h2></div>
    <div class="card-box">
      <el-table :data="reports" border stripe v-loading="loading">
        <el-table-column prop="projectName" label="所属项目" width="180" />
        <el-table-column prop="stageName" label="阶段" width="140" />
        <el-table-column prop="submitUserName" label="提交人" width="100" />
        <el-table-column prop="content" label="填报内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="progressRate" label="进度" width="80">
          <template #default="{row}">{{ row.progressRate }}%</template>
        </el-table-column>
        <el-table-column label="附件" width="80" align="center">
          <template #default="{row}">
            <el-icon v-if="row.attachmentName" style="color:#67c23a"><Paperclip /></el-icon>
            <span v-else style="color:#c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="130">
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPendingReviews } from '../api/report'

const router = useRouter()
const reports = ref([])
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await getPendingReviews()
    reports.value = res.data
  } finally { loading.value = false }
}

onMounted(loadData)
</script>
