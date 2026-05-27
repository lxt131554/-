<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      <h2 style="margin-top:8px">阶段填报</h2>
    </div>
    <div class="card-box" style="max-width:800px">
      <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
        <el-form-item label="进度" prop="progressRate">
          <el-slider v-model="form.progressRate" :min="0" :max="100" show-input style="width:300px" />
        </el-form-item>
        <el-form-item label="实际开始">
          <el-date-picker v-model="form.actualStart" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="实际结束">
          <el-date-picker v-model="form.actualEnd" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="工作内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请描述当前阶段的工作内容、进展" />
        </el-form-item>
        <el-form-item label="存在问题">
          <el-input v-model="form.problem" type="textarea" :rows="3" placeholder="当前遇到的问题或困难" />
        </el-form-item>
        <el-form-item label="偏差标记">
          <el-checkbox v-model="form.isDeviation">
            标记为偏差（工作延期或未按计划完成时勾选）
          </el-checkbox>
        </el-form-item>
        <el-form-item label="成果附件">
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
              <div style="color:#909399;font-size:12px">支持 PDF、Word、Excel、CAD图纸、图片、压缩包，单个文件不超过50MB</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交填报</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>

      <el-divider>历史填报记录</el-divider>
      <el-timeline v-if="history.length">
        <el-timeline-item v-for="item in history" :key="item.id" :timestamp="item.createTime" placement="top">
          <el-card>
            <p><strong>进度：</strong>{{ item.progressRate }}%</p>
            <p><strong>内容：</strong>{{ item.content }}</p>
            <p v-if="item.problem"><strong>问题：</strong>{{ item.problem }}</p>
            <p v-if="item.reviewComment"><strong>审阅意见：</strong>{{ item.reviewComment }}</p>
            <el-tag v-if="item.reviewStatus==='passed'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="item.reviewStatus==='returned'" type="danger" size="small">已退回</el-tag>
            <el-tag v-else type="info" size="small">待审阅</el-tag>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无历史记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../api/index'
import { getReports, submitReport } from '../api/report'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const stageId = route.params.stageId
const submitting = ref(false)
const formRef = ref(null)
const uploadRef = ref(null)
const history = ref([])
const selectedFile = ref(null)

const form = reactive({
  progressRate: 0, content: '', problem: '',
  actualStart: '', actualEnd: '', isDeviation: false
})
const rules = { content: [{ required: true, message: '请填写工作内容', trigger: 'blur' }] }

function handleFileChange(file) {
  selectedFile.value = file.raw
}
function handleFileRemove() {
  selectedFile.value = null
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
    if (selectedFile.value) {
      fd.append('file', selectedFile.value)
    }
    const res = await submitReport(stageId, fd)
    // 标记偏差时自动创建偏差记录
    if (form.isDeviation && form.problem) {
      try {
        await request.post('/deviations', {
          projectId: res.data.projectId || (history.value[0]?.projectId),
          description: form.problem,
          reason: '阶段执行偏差',
          impact: '影响项目进度'
        })
      } catch (e) { /* 静默处理 */ }
    }
    ElMessage.success('提交成功')
    router.back()
  } finally { submitting.value = false }
}

onMounted(loadHistory)
</script>
