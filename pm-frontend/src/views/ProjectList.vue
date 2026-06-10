<template>
  <div class="page-container">
    <div class="page-header">
      <h2>项目列表</h2>
    </div>
    <section class="page-summary-grid" style="margin-bottom:16px">
      <div class="summary-card summary-card--primary">
        <div class="summary-card-value">{{ total }}</div>
        <div class="summary-card-label">全部项目</div>
      </div>
      <div class="summary-card summary-card--success">
        <div class="summary-card-value">{{ activeCount }}</div>
        <div class="summary-card-label">进行中</div>
      </div>
      <div class="summary-card summary-card--primary">
        <div class="summary-card-value">{{ completedCount }}</div>
        <div class="summary-card-label">已完成</div>
      </div>
    </section>
    <div class="section-block">
      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="项目状态" clearable style="width:140px;margin-right:8px" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="进行中" value="active" />
          <el-option label="已完成" value="completed" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索项目名称" clearable style="width:280px" @clear="loadData" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div>
          <el-button type="primary" @click="router.push('/projects/create')" v-if="auth.user?.role==='manager'||auth.user?.role==='admin'">
            <el-icon><Plus /></el-icon> 新建项目
          </el-button>
        </div>
      </div>

      <el-table v-if="tableData.length" :data="tableData" v-loading="loading" class="pm-table">
        <el-table-column prop="id" label="编号" min-width="80" />
        <el-table-column prop="name" label="项目名称" min-width="200">
          <template #default="{row}">
            <el-link type="primary" @click="router.push(`/projects/${row.id}`)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{row}">
            <el-tag :type="row.status==='active'?'success':'info'" size="small">
              {{ row.status==='active'?'进行中': row.status==='completed'?'已完成':'已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{row}">
            <el-button text type="primary" @click="router.push(`/projects/${row.id}`)">查看</el-button>
            <el-button text type="danger" @click="handleDelete(row)" v-if="auth.user?.role==='admin'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无项目数据" />

      <el-pagination v-if="total > 0" v-model:current-page="page" :total="total" :page-size="size"
        layout="total, prev, pager, next" @current-change="loadData" style="margin-top:16px;justify-content:flex-end" />
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getProjects, deleteProject } from '../api/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const total = ref(0)
const size = ref(10)
const keyword = ref('')
const statusFilter = ref('')

const activeCount = computed(() => tableData.value.filter(r => r.status === 'active').length)
const completedCount = computed(() => tableData.value.filter(r => r.status === 'completed').length)

async function loadData() {
  loading.value = true
  try {
    const res = await getProjects({ page: page.value, size: size.value, keyword: keyword.value, status: statusFilter.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该项目？', '提示', { type: 'warning' })
  await deleteProject(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.pm-table {
  width: 100%;
}

.summary-card--primary {
  border-left: 3px solid var(--pm-accent);
}
.summary-card--primary .summary-card-value {
  color: var(--pm-accent);
}

.summary-card--success {
  border-left: 3px solid var(--pm-green-text);
}
.summary-card--success .summary-card-value {
  color: var(--pm-green-text);
}
</style>
