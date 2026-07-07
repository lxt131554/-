<template>
  <div class="page-container">
    <div class="page-header">
      <h2>偏差台账</h2>
    </div>
    <section class="page-summary-grid" style="margin-bottom:16px">
      <div class="summary-card summary-card--danger clickable" :class="{ active: filterStatus==='open' }" @click="setStatusFilter('open')">
        <div class="summary-card-value">{{ openCount }}</div>
        <div class="summary-card-label">未关闭偏差</div>
        <div class="summary-card-hint">需要跟进处理</div>
      </div>
      <div class="summary-card summary-card--success clickable" :class="{ active: filterStatus==='closed' }" @click="setStatusFilter('closed')">
        <div class="summary-card-value">{{ closedCount }}</div>
        <div class="summary-card-label">已关闭</div>
        <div class="summary-card-hint">已处理完成的偏差</div>
      </div>
      <div class="summary-card summary-card--primary clickable" :class="{ active: filterStatus==='' }" @click="setStatusFilter('')">
        <div class="summary-card-value">{{ allData.length }}</div>
        <div class="summary-card-label">全部偏差</div>
      </div>
    </section>
    <div class="section-block">
      <div class="filter-bar">
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

      <el-table v-if="tableData.length" :data="tableData" v-loading="loading">
        <el-table-column prop="projectName" label="所属项目" min-width="180" />
        <el-table-column prop="stageName" label="阶段" min-width="120" />
        <el-table-column prop="type" label="来源" min-width="80" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.type=='auto'?'warning':'info'">
              {{ row.type=='auto'?'自动':'手动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="偏差描述" min-width="240" show-overflow-tooltip>
          <template #default="{row}">
            <el-link type="primary" @click="router.push(`/deviations/${row.id}`)">{{ row.description }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="偏差原因" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="90" align="center">
          <template #default="{row}">
            <el-tag :type="row.status=='open'?'danger':'success'" size="small">
              {{ row.status=='open'?'未关闭':'已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{row}">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="80" fixed="right" align="center">
          <template #default="{row}">
            <el-button v-if="row.status=='open' && (auth.user?.role=='manager'||auth.user?.role=='admin')" text type="primary" size="small" @click="handleClose(row)">
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无偏差记录" />
    </div>

    <!-- Create dialog -->
    <el-dialog v-model="showCreate" title="手动记录偏差" width="500px" :close-on-click-modal="false" append-to-body align-center :lock-scroll="true">
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
        <el-button type="primary" @click="handleCreate" :loading="creating">保存</el-button>
        <el-button @click="showCreate=false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import request from '../api/index'
import { getProjects } from '../api/project'
import { ElMessage } from 'element-plus'
import { confirmDanger, showActionError } from '../utils/actionGuards'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const allData = ref([])
const tableData = ref([])
const filterStatus = ref('')
const showCreate = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const projects = ref([])
const form = reactive({ projectId: null, description: '', reason: '', impact: '' })

const openCount = computed(() => allData.value.filter(d => d.status === 'open').length)
const closedCount = computed(() => allData.value.filter(d => d.status === 'closed').length)

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/deviations')
    allData.value = res.data || []
    let list = allData.value
    if (filterStatus.value) list = list.filter(d => d.status === filterStatus.value)
    tableData.value = list
  } catch (error) {
    showActionError(error, '偏差台账加载失败')
  } finally { loading.value = false }
}

function setStatusFilter(status) {
  filterStatus.value = status
  loadData()
}

function formatTime(val) {
  if (!val) return '-'
  return val.substring(0, 16).replace('T', ' ')
}

async function handleClose(row) {
  try {
    await confirmDanger('确认关闭该偏差？关闭后将记录为已处理。')
    await request.put(`/deviations/${row.id}/close`)
    ElMessage.success('偏差已关闭')
    loadData()
  } catch (error) {
    showActionError(error, '关闭偏差失败')
  }
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
  } catch (error) {
    showActionError(error, '偏差记录保存失败')
  } finally { creating.value = false }
}

onMounted(async () => {
  loadData()
  if (auth.user?.role === 'manager' || auth.user?.role === 'admin') {
    try {
      const res = await getProjects({ page: 1, size: 100 })
      projects.value = res.data.records || []
    } catch (error) { showActionError(error, '项目列表加载失败') }
  }
})
</script>

<style scoped>
.summary-card.clickable.active {
  border-color: var(--pm-accent);
  box-shadow: inset 0 0 0 1px rgba(37,99,235,0.18), var(--pm-shadow);
}
</style>
