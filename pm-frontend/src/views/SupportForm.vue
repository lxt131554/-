<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      <h2 style="margin-top:8px">{{ isEdit ? '处理支持事项' : '新建支持申请' }}</h2>
    </div>

    <!-- Create mode -->
    <div class="card-box" style="max-width:700px" v-if="!isEdit">
      <el-form :model="form" label-width="90px" ref="formRef">
        <el-form-item label="所属项目" required>
          <el-select v-model="form.projectId" placeholder="选择项目" style="width:100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="事项标题" required>
          <el-input v-model="form.title" placeholder="简要描述需要支持的事项" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="详细说明需要的支持和原因" />
        </el-form-item>
        <el-form-item label="处理人">
          <el-select v-model="form.handlerId" placeholder="选择处理人" style="width:100%">
            <el-option v-for="u in users" :key="u.id" :label="u.realName + '（' + u.dept + '）'" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="期望时间">
          <el-date-picker v-model="form.expectTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Edit/View mode -->
    <div class="card-box" style="max-width:700px" v-else>
      <el-descriptions title="事项详情" :column="2" border v-if="detail">
        <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="所属项目">{{ detail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status=='pending'?'warning':'success'" size="small">
            {{ detail.status=='pending'?'待处理':'已解决' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ detail.handlerName || '未指定' }}</el-descriptions-item>
        <el-descriptions-item label="期望时间">{{ detail.expectTime || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="详细描述" :span="2">{{ detail.content || '无' }}</el-descriptions-item>
        <el-descriptions-item label="处理回复" :span="2" v-if="detail.reply">{{ detail.reply }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="detail?.status=='pending' && (auth.user?.role=='manager'||auth.user?.role=='admin')" style="margin-top:24px">
        <el-form label-width="90px">
          <el-form-item label="处理回复" required>
            <el-input v-model="reply" type="textarea" :rows="4" placeholder="输入处理意见、解决方案或协调结果" />
          </el-form-item>
          <el-form-item label="解决情况说明">
            <el-input v-model="resolveNote" type="textarea" :rows="3"
              placeholder="例：经院领导协调，县林业局已提供所需资料，外业调查恢复正常" />
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="handleResolve" :loading="submitting">
              <el-icon><Check /></el-icon> 确认解决
            </el-button>
            <el-button @click="$router.back()">返回</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div v-else style="margin-top:24px">
        <el-button @click="$router.back()">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import request from '../api/index'
import { getProjects } from '../api/project'
import { ElMessage } from 'element-plus'
import { confirmDanger, showActionError } from '../utils/actionGuards'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isEdit = ref(!!route.params.id)
const formRef = ref(null)
const submitting = ref(false)
const projects = ref([])
const users = ref([])
const detail = ref(null)
const reply = ref('')
const resolveNote = ref('')

const form = reactive({ projectId: null, title: '', content: '', handlerId: null, expectTime: '' })

async function loadFormData() {
  try {
    const [projRes] = await Promise.all([
      getProjects({ page: 1, size: 100 })
    ])
    projects.value = projRes.data.records || []
  } catch (error) {
    showActionError(error, '项目列表加载失败')
  }
}

async function loadDetail() {
  try {
    const res = await request.get('/supports')
    detail.value = (res.data || []).find(s => s.id == route.params.id)
  } catch (error) {
    showActionError(error, '支持事项详情加载失败')
  }
}

async function handleSubmit() {
  if (!form.projectId || !form.title) { ElMessage.warning('请填写项目名称和事项标题'); return }
  submitting.value = true
  try {
    await request.post('/supports', { ...form })
    ElMessage.success('支持申请已提交')
    router.back()
  } catch (error) {
    showActionError(error, '提交支持申请失败')
  } finally { submitting.value = false }
}

async function handleResolve() {
  if (!reply.value.trim()) { ElMessage.warning('请填写处理回复'); return }
  submitting.value = true
  try {
    await confirmDanger('确认将该支持事项标记为已解决？')
    await request.put(`/supports/${route.params.id}/resolve`, { reply: reply.value, resolveNote: resolveNote.value })
    ElMessage.success('已标记为已解决')
    router.back()
  } catch (error) {
    showActionError(error, '解决支持事项失败')
  } finally { submitting.value = false }
}

onMounted(() => {
  if (isEdit.value) { loadDetail() }
  else { loadFormData() }
})
</script>
