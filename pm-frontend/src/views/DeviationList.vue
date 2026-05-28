<template>
  <div class="page-container">
    <div class="page-header">
      <h2>偏差台账</h2>
    </div>
    <div class="card-box">
      <div class="page-toolbar">
        <div>
          <el-radio-group v-model="filterStatus" @change="loadData" size="small">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="open">未关闭</el-radio-button>
            <el-radio-button value="closed">已关闭</el-radio-button>
          </el-radio-group>
        </div>
        <el-button type="primary" @click="showCreate=true" v-if="auth.user?.role=='manager'||auth.user?.role=='admin'">
          <el-icon><Plus /></el-icon> 手动记录偏差
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" empty-text="暂无偏差记录">
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="stageName" label="阶段" min-width="120" />
        <el-table-column prop="type" label="来源" min-width="80" align="center">
          <template #default="{row}">
            <el-tag size="small" class="dev-tag" :class="'dev-tag--' + (row.type=='auto'?'amber':'slate')">
              {{ row.type=='auto'?'自动':'手动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="偏差描述" min-width="240" show-overflow-tooltip />
        <el-table-column prop="reason" label="偏差原因" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="90" align="center">
          <template #default="{row}">
            <el-tag :type="row.status=='open'?'danger':'success'" size="small">
              {{ row.status=='open'?'未关闭':'已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" min-width="80" fixed="right" align="center">
          <template #default="{row}">
            <el-button v-if="row.status=='open'" text type="primary" size="small" @click="handleClose(row)">
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Create dialog -->
    <el-dialog v-model="showCreate" title="手动记录偏差" width="500px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px" ref="createFormRef">
        <el-form-item label="所属项目" required>
          <el-select v-model="form.projectId" placeholder="选择项目" style="width:100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="偏差描述" required>
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="描述具体偏差情况" />
        </el-form-item>
        <el-form-item label="偏差原因">
          <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="分析偏差产生的原因" />
        </el-form-item>
        <el-form-item label="影响范围">
          <el-input v-model="form.impact" type="textarea" :rows="2" placeholder="对工期、质量、成本的影响" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate=false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import request from '../api/index'
import { getProjects } from '../api/project'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const loading = ref(false)
const tableData = ref([])
const filterStatus = ref('')
const showCreate = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const projects = ref([])
const form = reactive({ projectId: null, description: '', reason: '', impact: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/deviations')
    let list = res.data || []
    if (filterStatus.value) list = list.filter(d => d.status === filterStatus.value)
    tableData.value = list
  } finally { loading.value = false }
}

async function handleClose(row) {
  await request.put(`/deviations/${row.id}/close`)
  ElMessage.success('偏差已关闭')
  loadData()
}

async function handleCreate() {
  if (!form.projectId || !form.description) { ElMessage.warning('请填写项目名称和偏差描述'); return }
  creating.value = true
  try {
    await request.post('/deviations', { ...form })
    ElMessage.success('偏差记录已保存')
    showCreate.value = false
    Object.assign(form, { projectId: null, description: '', reason: '', impact: '' })
    loadData()
  } finally { creating.value = false }
}

onMounted(async () => {
  loadData()
  if (auth.user?.role === 'manager' || auth.user?.role === 'admin') {
    try {
      const res = await getProjects({ page: 1, size: 100 })
      projects.value = res.data.records || []
    } catch {}
  }
})
</script>

<style scoped>
.dev-tag {
  border-radius: 99px;
  font-size: 13px;
  letter-spacing: 0.03em;
  font-weight: 500;
  padding: 2px 10px;
  height: 20px;
  line-height: 18px;
  border: none;
  display: inline-block;
}

.dev-tag--amber {
  background: var(--pm-pale-amber);
  color: var(--pm-amber-text);
}

.dev-tag--slate {
  background: var(--pm-pale-slate);
  color: var(--pm-slate-text);
}
</style>
