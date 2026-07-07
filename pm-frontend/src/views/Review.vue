<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      <h2 style="margin-top:8px">审阅填报</h2>
    </div>
    <div class="card-box" style="max-width:1200px; width:100%">
      <el-descriptions title="填报详情" :column="2" border>
        <el-descriptions-item label="提交人">{{ report.submitUserName }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ report.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="进度">{{ report.progressRate }}%</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="report.reviewStatus==='pending'" type="warning" size="small">待审阅</el-tag>
          <el-tag v-else-if="report.reviewStatus==='passed'" type="success" size="small">已通过</el-tag>
          <el-tag v-else-if="report.reviewStatus==='returned'" type="danger" size="small">已退回</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="工作内容" :span="2">{{ report.content }}</el-descriptions-item>
        <el-descriptions-item label="存在问题" :span="2">{{ report.problem || '无' }}</el-descriptions-item>
        <el-descriptions-item label="审阅意见" :span="2" v-if="report.reviewComment">{{ report.reviewComment }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="report.reviewStatus==='pending'" style="margin-top:24px">
        <el-form label-width="80px">
          <el-form-item label="审阅意见">
            <el-input v-model="comment" type="textarea" :rows="3" placeholder="选填，退回时建议填写原因" />
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="doReview('passed')" :loading="submitting">
              <el-icon><Check /></el-icon> 通过
            </el-button>
            <el-button type="danger" @click="doReview('returned')" :loading="submitting">
              <el-icon><Close /></el-icon> 退回
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPendingReviews, reviewReport } from '../api/report'
import { ElMessage } from 'element-plus'
import { confirmDanger, showActionError } from '../utils/actionGuards'

const route = useRoute()
const router = useRouter()
const reportId = route.params.reportId
const report = ref({})
const comment = ref('')
const submitting = ref(false)

async function loadReport() {
  try {
    const res = await getPendingReviews()
    const found = res.data.find(r => r.id == reportId)
    if (found) report.value = found
    else ElMessage.warning('未找到该待审阅填报，可能已被处理')
  } catch (error) {
    showActionError(error, '填报详情加载失败')
  }
}
async function doReview(status) {
  if (status === 'returned' && !comment.value) {
    ElMessage.warning('退回时建议填写审阅意见')
  }
  submitting.value = true
  try {
    await confirmDanger(status === 'passed' ? '确认通过该填报？' : '确认退回该填报？')
    await reviewReport(reportId, { reviewStatus: status, reviewComment: comment.value })
    ElMessage.success(status === 'passed' ? '已通过' : '已退回')
    router.back()
  } catch (error) {
    showActionError(error, '审阅操作失败')
  } finally { submitting.value = false }
}

onMounted(loadReport)
</script>
