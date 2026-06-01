<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回项目</el-button>
      <h2 style="margin-top:8px">成果评审与审批</h2>
      <p style="color:var(--pm-text-secondary);font-size:14px">{{ projectName }}</p>
    </div>
    <div class="card-box" style="max-width:800px">
      <el-form :model="form" label-width="160px" ref="formRef">
        <el-form-item label="上级主管/专家评审情况">
          <el-input v-model="form.reviewSituation" type="textarea" :rows="5"
            placeholder="描述上级主管部门或专家评审的情况，包括评审组织、参与人员、评审意见等" />
        </el-form-item>
        <el-form-item label="评审未通过原因">
          <el-input v-model="form.failReason" type="textarea" :rows="3"
            placeholder="如评审有未通过情况，记录具体原因和需要修改的内容" />
        </el-form-item>
        <el-form-item label="最终确认时间">
          <el-date-picker v-model="form.confirmTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../api/index'
import { getProjectDetail } from '../api/project'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const projectId = route.params.id
const projectName = ref('')
const saving = ref(false)
const form = reactive({ reviewSituation: '', failReason: '', confirmTime: '' })

async function loadData() {
  try {
    const proj = await getProjectDetail(projectId)
    projectName.value = proj.data.name
    const res = await request.get(`/projects/${projectId}/approval`)
    if (res.data) Object.assign(form, res.data)
  } catch {}
}
async function handleSave() {
  saving.value = true
  try {
    await request.post(`/projects/${projectId}/approval`, { ...form })
    ElMessage.success('评审记录已保存')
    router.back()
  } finally { saving.value = false }
}

onMounted(loadData)
</script>
