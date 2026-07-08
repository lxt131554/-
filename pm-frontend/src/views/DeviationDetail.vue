<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      <h2>偏差详情</h2>
    </div>
    <div class="card-box">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="所属项目">{{ detail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="关联阶段">{{ detail.stageName || '无' }}</el-descriptions-item>
        <el-descriptions-item label="来源">
          <el-tag size="small" :type="detail.type=='auto'?'warning':'info'">{{ detail.type=='auto'?'自动生成':'手动记录' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="detail.status=='open'?'danger':'success'">{{ detail.status=='open'?'未关闭':'已关闭' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="偏差描述" :span="2">{{ detail.description }}</el-descriptions-item>
        <el-descriptions-item label="偏差原因" :span="2">{{ detail.reason || '无' }}</el-descriptions-item>
        <el-descriptions-item label="影响范围" :span="2">{{ detail.impact || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detail.createUserName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detail?.status=='open' && (auth.user?.role=='manager'||auth.user?.role=='admin')" style="margin-top:24px">
        <el-button type="success" @click="handleClose" :loading="closing">关闭偏差</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import request from '../api/index'
import { ElMessage } from 'element-plus'
import { confirmDanger, showActionError } from '../utils/actionGuards'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const detail = ref(null)
const closing = ref(false)

async function loadDetail() {
  try {
    const res = await request.get(`/deviations/${route.params.id}`)
    detail.value = res.data
  } catch (error) {
    showActionError(error, '偏差详情加载失败')
  }
}

async function handleClose() {
  closing.value = true
  try {
    await confirmDanger('确认关闭该偏差？关闭后将记录为已处理。')
    await request.put(`/deviations/${route.params.id}/close`)
    ElMessage.success('偏差已关闭')
    router.back()
  } catch (error) {
    showActionError(error, '关闭偏差失败')
  } finally { closing.value = false }
}

onMounted(loadDetail)
</script>
