<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回项目</el-button>
      <h2 style="margin-top:8px">项目自评</h2>
      <p style="color:var(--pm-text-secondary);font-size:14px">{{ projectName }}</p>
    </div>
    <div class="card-box" style="max-width:1200px; width:100%">
      <el-form :model="form" label-width="130px" ref="formRef">
        <el-form-item label="项目整体执行偏差">
          <el-input v-model="form.overallDeviation" type="textarea" :rows="4" placeholder="描述项目整体执行偏差情况，如：外业延迟15天、内业延迟30天、成果延迟30天" />
        </el-form-item>
        <el-form-item label="上级支持事项自评">
          <el-input v-model="form.supportEvaluation" type="textarea" :rows="3" placeholder="评价项目过程中上级支持事项的提出、响应、协调和解决情况" />
        </el-form-item>
        <el-form-item label="项目综合效率评价">
          <el-input v-model="form.efficiencyRating" type="textarea" :rows="3" placeholder="评价项目综合效率，如人力资源利用、时间管理、成本控制等方面" />
        </el-form-item>
        <el-form-item label="项目质量评价">
          <el-input v-model="form.qualityRating" type="textarea" :rows="3" placeholder="评价项目成果质量，如数据准确性、报告完整性、图件规范性等" />
        </el-form-item>
        <el-form-item label="全过程沟通情况">
          <el-input v-model="form.communicationNote" type="textarea" :rows="3" placeholder="描述项目全过程的内外部沟通情况，包括与业主的沟通、院内协调等" />
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
  projectId: null, overallDeviation: '', supportEvaluation: '', efficiencyRating: '', qualityRating: '', communicationNote: ''
})

async function loadData() {
  try {
    const proj = await getProjectDetail(projectId)
    projectName.value = proj.data.name
    const res = await request.get(`/projects/${projectId}/review`)
    if (res.data) {
      Object.assign(form, res.data)
    }
    form.projectId = parseInt(projectId)
  } catch (error) {
    showActionError(error, '项目自评加载失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    await request.post(`/projects/${projectId}/review`, { ...form, projectId: parseInt(projectId) })
    ElMessage.success('自评已保存')
    router.back()
  } catch (error) {
    showActionError(error, '项目自评保存失败')
  } finally { saving.value = false }
}

onMounted(loadData)
</script>
