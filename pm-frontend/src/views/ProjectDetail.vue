<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.push('/projects')"><el-icon><ArrowLeft /></el-icon> 返回列表</el-button>
      <h2 style="margin-top:8px">{{ project.name }}</h2>
      <p style="color:var(--pm-text-secondary);margin-top:4px">{{ project.description || '暂无描述' }}</p>
      <el-button v-if="project.status=='active' && (auth.user?.role=='manager'||auth.user?.role=='admin')"
        type="warning" size="small" @click="handleCompleteProject" style="margin-left:12px">
        完成项目
      </el-button>
    </div>

    <div class="card-box" style="margin-bottom:16px">
      <div class="page-toolbar">
        <span class="section-title">项目阶段</span>
        <el-button type="primary" size="small" @click="showAddStage=true"
          v-if="auth.user?.role==='manager'||auth.user?.role==='admin'">
          <el-icon><Plus /></el-icon> 添加阶段
        </el-button>
      </div>
      <el-table :data="stages">
        <el-table-column prop="stageName" label="阶段名称" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="assigneeName" label="责任人" min-width="120" />
        <el-table-column label="计划时间" min-width="200">
          <template #default="{row}">{{ row.planStart || '-' }} 至 {{ row.planEnd || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="110">
          <template #default="{row}">
            <el-tag v-if="row.status==='pending'" type="info" size="small">待开始</el-tag>
            <el-tag v-else-if="row.status==='in_progress'" type="primary" size="small">进行中</el-tag>
            <el-tag v-else-if="row.status==='submitted'" type="warning" size="small">待审阅</el-tag>
            <el-tag v-else-if="row.status==='completed'" type="success" size="small">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最新填报" min-width="200" show-overflow-tooltip>
          <template #default="{row}">{{ row.latestReport?.content || '暂无' }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="180">
          <template #default="{row}">
            <el-button text type="primary" @click="router.push(`/my-tasks/${row.id}/report`)"
              v-if="auth.user?.role==='engineer'">填报</el-button>
            <el-button text type="danger" @click="handleDeleteStage(row)" v-if="auth.user?.role==='admin'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card-box">
      <div class="page-toolbar">
        <span class="section-title">项目成员</span>
        <el-button type="primary" size="small" @click="showAddMember=true">
          <el-icon><Plus /></el-icon> 添加成员
        </el-button>
      </div>
      <div class="member-tags">
        <el-tag v-for="m in members" :key="m.id" closable :type="m.roleInProject==='manager'?'warning':'success'"
          @close="handleRemoveMember(m)" size="large">
          {{ m.realName }}（{{ m.roleInProject==='manager'?'负责人':'工程师' }}）
        </el-tag>
      </div>
    </div>

    <div class="card-box" v-if="project.status=='completed'" style="margin-top:16px">
      <div class="page-toolbar">
        <span style="font-weight:600;font-size:16px">收尾复盘</span>
      </div>
      <div style="display:flex;gap:12px">
        <el-button type="primary" @click="router.push(`/projects/${projectId}/review`)">
          项目自评
        </el-button>
        <el-button type="primary" @click="router.push(`/projects/${projectId}/experience`)">
          经验总结
        </el-button>
      </div>
    </div>

    <el-dialog v-model="showAddStage" title="添加阶段" width="500px">
      <el-form :model="stageForm" label-width="80px">
        <el-form-item label="阶段名称" required>
          <el-input v-model="stageForm.stageName" placeholder="如：外业调查" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="stageForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="责任人" required>
          <el-select v-model="stageForm.assigneeId" placeholder="选择责任人" style="width:100%">
            <el-option v-for="m in members" :key="m.userId" :label="m.realName" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始">
          <el-date-picker v-model="stageForm.planStart" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="计划结束">
          <el-date-picker v-model="stageForm.planEnd" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="stageForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddStage=false">取消</el-button>
        <el-button type="primary" @click="handleAddStage">添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAddMember" title="添加成员" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户ID">
          <el-input v-model="newMember.userId" placeholder="输入用户ID" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="newMember.roleInProject" style="width:100%">
            <el-option label="项目负责人" value="manager" />
            <el-option label="工程师" value="engineer" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddMember=false">取消</el-button>
        <el-button type="primary" @click="handleAddMember">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getProjectDetail, updateProject } from '../api/project'
import { getStages, addStage, deleteStage } from '../api/stage'
import { getProjectMembers, addProjectMember, removeProjectMember } from '../api/project'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const projectId = route.params.id

const project = ref({})
const stages = ref([])
const members = ref([])
const showAddStage = ref(false)
const showAddMember = ref(false)

const stageForm = reactive({
  stageName: '', description: '', assigneeId: null,
  planStart: '', planEnd: '', sortOrder: 0
})
const newMember = reactive({ userId: '', roleInProject: 'engineer' })

async function loadProject() {
  const res = await getProjectDetail(projectId)
  project.value = res.data
}
async function loadStages() {
  const res = await getStages(projectId)
  stages.value = res.data
}
async function loadMembers() {
  const res = await getProjectMembers(projectId)
  members.value = res.data
}

async function handleAddStage() {
  if (!stageForm.stageName || !stageForm.assigneeId) {
    ElMessage.warning('请填写阶段名称和责任人')
    return
  }
  await addStage(projectId, { ...stageForm })
  ElMessage.success('阶段添加成功')
  showAddStage.value = false
  Object.assign(stageForm, { stageName: '', description: '', assigneeId: null, planStart: '', planEnd: '', sortOrder: 0 })
  loadStages()
}
async function handleDeleteStage(row) {
  await deleteStage(projectId, row.id)
  ElMessage.success('删除成功')
  loadStages()
}
async function handleAddMember() {
  if (!newMember.userId) { ElMessage.warning('请输入用户ID'); return }
  await addProjectMember(projectId, { ...newMember })
  ElMessage.success('成员添加成功')
  showAddMember.value = false
  newMember.userId = ''
  loadMembers()
}
async function handleRemoveMember(m) {
  await removeProjectMember(projectId, m.id)
  ElMessage.success('移除成功')
  loadMembers()
}

async function handleCompleteProject() {
  try {
    await updateProject(projectId, { ...project.value, status: 'completed' })
    ElMessage.success('项目已完成')
    loadProject()
  } catch {}
}

onMounted(() => { loadProject(); loadStages(); loadMembers() })
</script>

<style scoped>
.section-title {
  font-weight: 600;
  font-size: 16px;
  color: var(--pm-text);
  letter-spacing: -0.01em;
}

.member-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
