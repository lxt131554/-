<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回项目</el-button>
      <h2 style="margin-top:8px">经验总结</h2>
      <p style="color:var(--pm-text-secondary);font-size:14px">{{ projectName }}</p>
    </div>
    <div class="card-box" style="max-width:1200px; width:100%">
      <el-form :model="form" label-width="130px" ref="formRef">
        <el-form-item label="值得复用的经验">
          <el-input v-model="form.reusableExperience" type="textarea" :rows="4" placeholder="总结本项目中可以复用的成功经验和方法" />
        </el-form-item>
        <el-form-item label="相关短板或缺陷">
          <el-input v-model="form.shortcomings" type="textarea" :rows="3" placeholder="总结项目执行过程中暴露的短板和不足之处" />
        </el-form-item>
        <el-form-item label="风险点">
          <el-input v-model="form.risks" type="textarea" :rows="3" placeholder="记录项目中遇到或识别的风险点，供后续项目参考" />
        </el-form-item>
        <el-form-item label="改进建议">
          <el-input v-model="form.improvement" type="textarea" :rows="3" placeholder="提出针对性的改进建议，帮助后续项目避免类似问题" />
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
import { showActionError } from '../utils/actionGuards'

const route = useRoute()
const router = useRouter()
const projectId = route.params.id
const projectName = ref('')
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  projectId: null, reusableExperience: '', shortcomings: '', risks: '', improvement: ''
})

async function loadData() {
  try {
    const proj = await getProjectDetail(projectId)
    projectName.value = proj.data.name
    const res = await request.get(`/projects/${projectId}/experience`)
    if (res.data) {
      Object.assign(form, res.data)
    }
    form.projectId = parseInt(projectId)
  } catch (error) {
    showActionError(error, '经验总结加载失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    await request.post(`/projects/${projectId}/experience`, { ...form, projectId: parseInt(projectId) })
    ElMessage.success('经验总结已保存')
    router.back()
  } catch (error) {
    showActionError(error, '经验总结保存失败')
  } finally { saving.value = false }
}

onMounted(loadData)
</script>
