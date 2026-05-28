<template>
  <div class="page-container">
    <div class="page-header"><h2>我的待填报</h2></div>
    <div class="card-box">
      <el-table :data="tasks" v-loading="loading">
        <el-table-column prop="stageName" label="阶段名称" min-width="160" />
        <el-table-column prop="description" label="阶段描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="计划时间" min-width="200">
          <template #default="{row}">{{ row.planStart || '-' }} 至 {{ row.planEnd || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="110">
          <template #default="{row}">
            <el-tag v-if="row.status==='pending'" type="info" size="small">待填报</el-tag>
            <el-tag v-else-if="row.status==='in_progress'" type="warning" size="small">待重新填报</el-tag>
            <el-tag v-else-if="row.status==='submitted'" type="primary" size="small">待审阅</el-tag>
            <el-tag v-else-if="row.status==='completed'" type="success" size="small">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120">
          <template #default="{row}">
            <el-button type="primary" size="small" @click="router.push(`/my-tasks/${row.id}/report`)">填报</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tasks.length===0" description="暂无需填报的阶段" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyTasks } from '../api/stage'

const router = useRouter()
const tasks = ref([])
const loading = ref(false)

async function loadTasks() {
  loading.value = true
  try {
    const res = await getMyTasks()
    tasks.value = res.data
  } finally { loading.value = false }
}

onMounted(loadTasks)
</script>
