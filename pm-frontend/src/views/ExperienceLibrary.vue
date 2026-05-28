<template>
  <div class="page-container">
    <div class="page-header"><h2>经验库</h2></div>
    <div class="card-box">
      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无经验总结">
        <el-table-column prop="projectName" label="项目名称" min-width="200" />
        <el-table-column prop="reusableExperience" label="值得复用的经验" min-width="240" show-overflow-tooltip />
        <el-table-column prop="shortcomings" label="短板或缺陷" min-width="200" show-overflow-tooltip />
        <el-table-column prop="improvement" label="改进建议" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/index'

const tableData = ref([])
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/experiences')
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(loadData)
</script>
