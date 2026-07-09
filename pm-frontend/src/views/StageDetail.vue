<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      <h2>{{ stage.stageName }}</h2>
      <p style="color:var(--pm-text-secondary);margin-top:4px">
        所属项目：<el-link type="primary" @click="router.push(`/projects/${stage.projectId}`)">{{ stage.projectName }}</el-link>
        &nbsp;|&nbsp; 责任人：{{ stage.assigneeName || '未指定' }}
        &nbsp;|&nbsp; 状态：<el-tag size="small">{{ statusLabel }}</el-tag>
      </p>
    </div>

    <!-- Basic info -->
    <div class="card-box" style="margin-bottom:16px">
      <div style="font-weight:600;font-size:16px;margin-bottom:12px">基本信息</div>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="阶段描述">{{ stage.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="计划时间">{{ stage.planStart || '-' }} 至 {{ stage.planEnd || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际开始">{{ stage.actualStart || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际结束">{{ stage.actualEnd || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- Deviation summary -->
    <div class="card-box" style="margin-bottom:16px" v-if="deviations.length">
      <div style="font-weight:600;font-size:16px;margin-bottom:12px">偏差记录</div>
      <div v-for="d in deviations" :key="d.id" style="padding:12px;border:1px solid var(--pm-border);border-radius:8px;margin-bottom:8px">
        <el-tag :type="d.status=='open'?'danger':'success'" size="small" style="margin-bottom:8px">{{ d.status=='open'?'未关闭':'已关闭' }}</el-tag>
        <div style="margin-bottom:4px"><strong>偏差描述：</strong>{{ d.description }}</div>
        <div style="margin-bottom:4px" v-if="d.reason"><strong>原因：</strong>{{ d.reason }}</div>
        <div v-if="d.impact"><strong>影响：</strong>{{ d.impact }}</div>
      </div>
    </div>

    <!-- Reports history -->
    <div class="card-box">
      <div style="font-weight:600;font-size:16px;margin-bottom:12px">填报记录（{{ reports.length }}条）</div>
      <div v-if="!reports.length" style="color:var(--pm-text-muted);text-align:center;padding:40px">暂无填报记录</div>
      <div v-for="(r, i) in reports" :key="r.id" style="padding:16px;border:1px solid var(--pm-border);border-radius:8px;margin-bottom:12px">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
          <span style="font-weight:600">第 {{ reports.length - i }} 次填报</span>
          <span style="font-size:13px;color:var(--pm-text-muted)">{{ r.submitUserName }} · {{ r.submitTime }}</span>
        </div>
        <div style="margin-bottom:4px"><strong>进度：</strong>{{ r.progressRate }}%</div>
        <div style="margin-bottom:4px"><strong>工作内容：</strong>{{ r.content || '无' }}</div>
        <div style="margin-bottom:4px" v-if="r.problem"><strong>存在问题：</strong>{{ r.problem }}</div>
        <div style="margin-bottom:4px" v-if="r.qualityControl"><strong>质量管控：</strong>{{ r.qualityControl }}</div>
        <div style="margin-bottom:4px" v-if="r.resultSummary"><strong>阶段成果：</strong>{{ r.resultSummary }}</div>
        <div style="margin-bottom:4px" v-if="r.coordinationNote"><strong>沟通协调：</strong>{{ r.coordinationNote }}</div>
        <div style="margin-bottom:4px" v-if="r.deptReviewNote"><strong>部门审核：</strong>{{ r.deptReviewNote }}</div>
        <div v-if="r.attachmentName">
          <strong>附件：</strong><el-link type="primary" :underline="false" @click="downloadAttachment(r.id)">{{ r.attachmentName }}</el-link>
        </div>
        <div v-if="r.reviewComment" style="margin-top:8px;padding:8px;background:var(--pm-bg);border-radius:4px">
          <strong>审阅意见：</strong>{{ r.reviewComment }}
          <el-tag size="small" style="margin-left:8px">{{ r.reviewStatus=='passed'?'已通过':r.reviewStatus=='returned'?'已退回':'待审阅' }}</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../api/index'
import { showActionError } from '../utils/actionGuards'

const route = useRoute()
const router = useRouter()
const stage = ref({})
const reports = ref([])
const deviations = ref([])

const statusLabel = computed(() => {
  const map = { pending:'待开始', in_progress:'进行中', submitted:'待审阅', completed:'已完成' }
  return map[stage.value.status] || stage.value.status
})

async function loadData() {
  try {
    const res = await request.get(`/stages/${route.params.id}/detail`)
    stage.value = res.data.stage
    reports.value = res.data.reports || []
    deviations.value = res.data.deviations || []
  } catch (error) {
    showActionError(error, '阶段详情加载失败')
  }
}

function downloadAttachment(reportId) {
  window.open('/api/reports/' + reportId + '/attachment', '_blank')
}

onMounted(loadData)
</script>
