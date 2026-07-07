<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      <h2 style="margin-top:8px; font-size:22px">阶段填报</h2>
    </div>
    <div class="card-box" style="max-width:1100px; width:calc(100vw - 64px)">
      <el-form :model="form" ref="formRef" :rules="rules">

        <!-- Group 1: 进度与时间 -->
        <el-divider content-position="left">进度与时间</el-divider>
        <div class="progress-grid" style="display:grid; grid-template-columns: 1fr 1fr 1fr; gap:18px; margin-bottom:20px">
          <div>
            <div style="font-weight:500; margin-bottom:6px; font-size:14px">完成进度</div>
            <el-slider v-model="form.progressRate" :min="0" :max="100" show-input />
          </div>
          <div>
            <div style="font-weight:500; margin-bottom:6px; font-size:14px">实际开始时间</div>
            <el-date-picker v-model="form.actualStart" type="date" style="width:100%" value-format="YYYY-MM-DD" />
          </div>
          <div>
            <div style="font-weight:500; margin-bottom:6px; font-size:14px">实际结束时间</div>
            <el-date-picker v-model="form.actualEnd" type="date" style="width:100%" value-format="YYYY-MM-DD" />
          </div>
        </div>

        <!-- Group 2: 工作内容 -->
        <el-divider content-position="left">工作内容</el-divider>
        <div style="margin-bottom:20px">
          <div style="font-weight:500; margin-bottom:6px; font-size:14px">当前完成情况 <span style="color:var(--pm-red-text)">*</span></div>
          <el-input v-model="form.content" type="textarea" :rows="4" style="min-height:96px" placeholder="请描述当前阶段的工作内容、进展" />
        </div>
        <div style="margin-bottom:20px">
          <div style="font-weight:500; margin-bottom:6px; font-size:14px">沟通协调情况</div>
          <el-input v-model="form.coordinationNote" type="textarea" :rows="3" style="min-height:96px" placeholder="例：与县林业局对接3次；院内部协调会2次；业主反馈需调整样地布局" />
        </div>

        <!-- Group 3: 风险与偏差 -->
        <el-divider content-position="left">风险与偏差</el-divider>
        <div style="margin-bottom:20px">
          <div style="font-weight:500; margin-bottom:6px; font-size:14px">存在问题或偏差原因</div>
          <el-input v-model="form.problem" type="textarea" :rows="3" style="min-height:96px" placeholder="当前遇到的问题或困难" />
        </div>
        <div style="margin-bottom:20px">
          <el-checkbox v-model="form.isDeviation">
            标记为偏差项（存在延期或未按计划完成时勾选，系统将自动生成偏差记录）
          </el-checkbox>
        </div>

        <!-- Group 4: 质量控制与成果 -->
        <el-divider content-position="left">质量控制与成果</el-divider>
        <div style="margin-bottom:20px">
          <div style="font-weight:500; margin-bottom:6px; font-size:14px">质量控制措施</div>
          <el-input v-model="form.qualityControl" type="textarea" :rows="3" style="min-height:96px" placeholder="例：每样地设置8个检尺点；外业数据当日复核；照片与样地编号一一对应" />
        </div>
        <div style="margin-bottom:20px">
          <div style="font-weight:500; margin-bottom:6px; font-size:14px">阶段成果说明</div>
          <el-input v-model="form.resultSummary" type="textarea" :rows="3" style="min-height:96px" placeholder="例：完成小班调查表120份、树种分布图2幅、蓄积量统计表1套" />
        </div>
        <el-alert v-if="!form.qualityControl || !form.resultSummary"
          title="建议填写质量控制措施和阶段成果说明，以便后续审核和台账导出"
          type="info" :closable="false" show-icon style="margin-bottom:20px" />

        <!-- Group 5: 附件与提交 -->
        <el-divider content-position="left">附件与提交</el-divider>
        <div style="margin-bottom:20px">
          <div style="font-weight:500; margin-bottom:6px; font-size:14px">部门审核情况</div>
          <el-input v-model="form.deptReviewNote" type="textarea" :rows="3" style="min-height:96px" placeholder="例：部门审核发现图件缺少比例尺标注、统计表数据与文字描述不一致，已退回修改" />
        </div>
        <div style="margin-bottom:8px">
          <div style="font-weight:500; margin-bottom:6px; font-size:14px">成果附件</div>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.dwg,.jpg,.png,.zip,.rar"
          >
            <el-button type="primary" plain>
              <el-icon><Upload /></el-icon> 选择文件
            </el-button>
            <template #tip>
              <div class="upload-tip">支持 PDF、Word、Excel、CAD图纸、图片、压缩包，单个文件不超过50MB</div>
            </template>
          </el-upload>
        </div>

        <!-- Guard message -->
        <el-alert v-if="submitGuardMessage" :title="submitGuardMessage" type="warning" :closable="false" show-icon style="margin-top:16px" />
      </el-form>

      <!-- Sticky bottom action bar -->
      <div class="sticky-action-bar">
        <el-button size="large" @click="router.back()">取消</el-button>
        <el-button v-if="submitGuardMessage" size="large" type="primary" disabled>{{ submitGuardMessage }}</el-button>
        <el-button v-else size="large" type="primary" @click="handleSubmit" :loading="submitting">提交审核</el-button>
      </div>

      <!-- History table -->
      <el-divider>历史填报记录</el-divider>
      <el-table v-if="history.length" :data="history" size="small" stripe>
        <el-table-column label="提交时间" width="160">
          <template #default="{row}">{{ row.createTime?.substring(0,16) }}</template>
        </el-table-column>
        <el-table-column prop="progressRate" label="进度" width="80" align="center">
          <template #default="{row}">{{ row.progressRate }}%</template>
        </el-table-column>
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{row}">
            <el-tag v-if="row.reviewStatus==='passed'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="row.reviewStatus==='returned'" type="danger" size="small">已退回</el-tag>
            <el-tag v-else type="info" size="small">待审阅</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="主要内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reviewComment" label="审核意见" min-width="180" show-overflow-tooltip>
          <template #default="{row}">
            <span v-if="row.reviewComment">{{ row.reviewComment }}</span>
            <span v-else style="color:var(--pm-text-muted)">—</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无历史记录" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../api/index'
import { getReports, submitReport } from '../api/report'
import { getProjectDetail } from '../api/project'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const stageId = route.params.stageId
const submitting = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const history = ref([])
const selectedFile = ref(null)

const stage = ref(null)
const project = ref(null)

const form = reactive({
  progressRate: 0, content: '', problem: '',
  actualStart: '', actualEnd: '', isDeviation: false,
  qualityControl: '', resultSummary: '', coordinationNote: '', deptReviewNote: ''
})
const rules = { content: [{ required: true, message: '请填写工作内容', trigger: 'blur' }] }

const currentUserId = computed(() => auth.user?.id)

const submitGuardMessage = computed(() => {
  if (!stage.value) return null
  if (project.value?.status === 'completed') return '项目已完成，无法提交'
  if (stage.value.status === 'submitted') return '阶段已提交，等待审阅'
  if (stage.value.assigneeId != null && stage.value.assigneeId !== currentUserId.value) return '仅阶段责任人可以提交'
  return null
})

function handleFileChange(file) {
  selectedFile.value = file.raw
}
function handleFileRemove() {
  selectedFile.value = null
}

async function loadStageDetail() {
  try {
    const res = await request.get(`/stages/${stageId}/detail`)
    if (res.data?.stage) {
      stage.value = res.data.stage
      project.value = { id: res.data.projectId, name: res.data.projectName }
      // Also load full project info for status check
      if (res.data.projectId) {
        try {
          const projRes = await getProjectDetail(res.data.projectId)
          if (projRes.data) {
            project.value = projRes.data
          }
        } catch { /* ignore project detail failures */ }
      }
    }
  } catch { /* handled by guard message */ }
}

async function loadHistory() {
  const res = await getReports(stageId)
  history.value = res.data
}
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('content', form.content)
    fd.append('progressRate', form.progressRate)
    fd.append('problem', form.problem || '')
    fd.append('actualStart', form.actualStart || '')
    fd.append('actualEnd', form.actualEnd || '')
    fd.append('qualityControl', form.qualityControl || '')
    fd.append('resultSummary', form.resultSummary || '')
    fd.append('coordinationNote', form.coordinationNote || '')
    fd.append('deptReviewNote', form.deptReviewNote || '')
    if (selectedFile.value) {
      fd.append('file', selectedFile.value)
    }
    const res = await submitReport(stageId, fd)
    // 标记偏差时自动创建偏差记录
    if (form.isDeviation && form.problem) {
      await request.post('/deviations', {
        projectId: res.data.projectId || (history.value[0]?.projectId),
        description: form.problem,
        reason: '阶段执行偏差',
        impact: '影响项目进度'
      })
    }
    ElMessage.success('提交成功')
    router.back()
  } finally { submitting.value = false }
}

onMounted(() => {
  loadStageDetail()
  loadHistory()
})
</script>

<style scoped>
.upload-tip {
  color: var(--pm-text-muted);
  font-size: 14px;
  margin-top: 6px;
}

.sticky-action-bar {
  position: sticky;
  bottom: 0;
  background: var(--pm-surface);
  border-top: 1px solid var(--pm-border);
  padding: 16px 24px;
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  z-index: 10;
}

@media (max-width: 768px) {
  .progress-grid {
    grid-template-columns: 1fr !important;
  }
}
</style>
