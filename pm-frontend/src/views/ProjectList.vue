<template>
  <div class="page-container">
    <div class="page-header">
      <h2>项目列表</h2>
    </div>
    <div class="card-box">
      <div class="page-toolbar">
        <el-input v-model="keyword" placeholder="搜索项目名称" clearable style="width:280px" @clear="loadData" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div>
          <el-button type="primary" @click="showCreate=true" v-if="auth.user?.role==='manager'||auth.user?.role==='admin'">
            <el-icon><Plus /></el-icon> 新建项目
          </el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" class="pm-table">
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

      <el-pagination v-model:current-page="page" :total="total" :page-size="size"
        layout="total, prev, pager, next" @current-change="loadData" style="margin-top:16px;justify-content:flex-end" />
    </div>

    <el-dialog v-model="showCreate" title="新建项目" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="80px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入项目描述（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate=false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getProjects, createProject, deleteProject } from '../api/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const total = ref(0)
const size = ref(10)
const keyword = ref('')

const showCreate = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const createForm = reactive({ name: '', description: '' })
const createRules = { name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }] }

async function loadData() {
  loading.value = true
  try {
    const res = await getProjects({ page: page.value, size: size.value, keyword: keyword.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleCreate() {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    await createProject({ name: createForm.name, description: createForm.description })
    ElMessage.success('项目创建成功')
    showCreate.value = false
    createForm.name = ''
    createForm.description = ''
    loadData()
  } finally { creating.value = false }
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
</style>
