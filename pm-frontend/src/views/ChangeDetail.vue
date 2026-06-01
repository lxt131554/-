<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      <h2 style="margin-top:8px">变更详情</h2>
    </div>
    <div class="card-box" style="max-width:900px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="所属项目">{{ detail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="确认时间">{{ detail.confirmTime || '未确认' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="detail.status=='confirmed'?'success':'warning'">{{ detail.status=='confirmed'?'已确认':'待确认' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="变更内容" :span="2">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item label="影响范围" :span="2">{{ detail.impact || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detail?.status=='pending' && (auth.user?.role=='leader'||auth.user?.role=='admin')" style="margin-top:24px">
        <el-button type="primary" @click="handleConfirm" :loading="confirming">确认变更</el-button>
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

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const detail = ref(null)
const confirming = ref(false)

async function loadDetail() {
  try {
    const res = await request.get(`/changes/${route.params.id}`)
    detail.value = res.data
  } catch {}
}

async function handleConfirm() {
  confirming.value = true
  try {
    await request.put(`/changes/${route.params.id}/confirm`)
    ElMessage.success('变更已确认')
    router.back()
  } finally { confirming.value = false }
}

onMounted(loadDetail)
</script>
