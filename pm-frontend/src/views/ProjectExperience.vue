<template>
  <div class="page-container">
      <div class="page-header">
        <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
        <h2>经验总结</h2>
        <p style="color:var(--pm-text-secondary);margin-top:4px">对项目执行过程进行经验总结，沉淀可复用的方法与改进方向</p>
      </div>

      <div class="review-layout">
        <!-- Left: help sidebar -->
        <aside class="review-aside">
          <h3>填写说明</h3>
          <p>经验总结是对整个项目执行过程中的成功经验、不足之处进行系统梳理，为后续项目提供参考。</p>
          <div style="margin-top:16px">
            <h3>总结维度</h3>
            <p>1. 值得复用的经验</p>
            <p>2. 相关短板或缺陷</p>
            <p>3. 风险点记录</p>
            <p>4. 改进建议</p>
          </div>
          <div style="margin-top:16px;font-size:13px;color:var(--pm-text-muted)">
            填写完成后请保存，经验总结将进入经验库供团队参考。
          </div>
        </aside>

        <!-- Right: form -->
        <div>
          <div class="review-form-card">
            <el-form :model="form" label-width="130px" ref="formRef">
              <el-form-item label="值得复用的经验">
                <el-input v-model="form.reusableExperience" type="textarea" :autosize="{ minRows: 5, maxRows: 14 }" placeholder="总结本项目中可以复用的成功经验和方法" />
                <div class="form-tip">总结可以复用到后续项目中的成功经验、技术方案或管理方法。</div>
              </el-form-item>
              <el-form-item label="相关短板或缺陷">
                <el-input v-model="form.shortcomings" type="textarea" :autosize="{ minRows: 5, maxRows: 14 }" placeholder="总结项目执行过程中暴露的短板和不足之处" />
                <div class="form-tip">总结项目实施过程中存在的不足、短板或需要改进的地方。</div>
              </el-form-item>
              <el-form-item label="风险点">
                <el-input v-model="form.risks" type="textarea" :autosize="{ minRows: 5, maxRows: 14 }" placeholder="记录项目中遇到或识别的风险点，供后续项目参考" />
                <div class="form-tip">总结项目执行中遇到的主要风险及其应对情况。</div>
              </el-form-item>
              <el-form-item label="改进建议">
                <el-input v-model="form.improvement" type="textarea" :autosize="{ minRows: 5, maxRows: 14 }" placeholder="提出针对性的改进建议，帮助后续项目避免类似问题" />
                <div class="form-tip">针对本次项目，提出具体的改进建议和优化方向。</div>
              </el-form-item>
            </el-form>
          </div>

          <!-- Bottom action bar -->
          <div class="action-bar">
            <span style="color:var(--pm-text-muted);font-size:13px">填写完成后请保存，经验总结将进入经验库供团队参考。</span>
            <div style="display:flex;gap:12px">
              <el-button @click="router.back()" size="large">返回项目</el-button>
              <el-button type="primary" @click="handleSave" :loading="saving" size="large">保存经验</el-button>
            </div>
          </div>
        </div>
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
