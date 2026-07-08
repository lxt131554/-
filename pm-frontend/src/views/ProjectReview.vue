<template>
  <div class="page-container">
    <div class="content-shell">
      <div class="page-header">
        <el-button text @click="router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
        <h2 style="margin-top:8px">项目自评</h2>
        <p style="color:var(--pm-text-secondary);margin-top:4px">对项目执行过程进行总结评估，用于项目复盘与经验沉淀</p>
      </div>

      <div class="review-layout">
        <!-- Left: help sidebar -->
        <aside class="review-aside">
          <h3>填写说明</h3>
          <p>项目自评是对整个项目执行过程进行回顾和总结。请根据实际情况如实填写以下内容。</p>
          <div style="margin-top:16px">
            <h3>自评维度</h3>
            <p>1. 项目整体执行偏差</p>
            <p>2. 上级支持事项自评</p>
            <p>3. 项目综合效率评价</p>
            <p>4. 项目质量评价</p>
            <p>5. 全过程沟通情况</p>
          </div>
          <div style="margin-top:16px;font-size:13px;color:var(--pm-text-muted)">
            填写完成后请保存，自评内容将用于项目复盘与经验沉淀。
          </div>
        </aside>

        <!-- Right: form -->
        <div>
          <div class="review-form-card">
            <el-form :model="form" label-width="130px" ref="formRef">
              <el-form-item label="项目整体执行偏差">
                <el-input v-model="form.overallDeviation" type="textarea" :autosize="{ minRows: 5, maxRows: 14 }" placeholder="描述项目整体执行偏差情况，如：外业延迟15天、内业延迟30天、成果延迟30天" />
                <div class="form-tip">填写项目计划、进度、质量、沟通等方面与预期目标的差异。</div>
              </el-form-item>
              <el-form-item label="上级支持事项自评">
                <el-input v-model="form.supportEvaluation" type="textarea" :autosize="{ minRows: 5, maxRows: 14 }" placeholder="评价项目过程中上级支持事项的提出、响应、协调和解决情况" />
                <div class="form-tip">填写上级协调、资源支持、审批反馈等方面的实际情况。</div>
              </el-form-item>
              <el-form-item label="项目综合效率评价">
                <el-input v-model="form.efficiencyRating" type="textarea" :autosize="{ minRows: 5, maxRows: 14 }" placeholder="评价项目综合效率，如人力资源利用、时间管理、成本控制等方面" />
                <div class="form-tip">填写项目执行效率、协同效率、问题响应效率等。</div>
              </el-form-item>
              <el-form-item label="项目质量评价">
                <el-input v-model="form.qualityRating" type="textarea" :autosize="{ minRows: 5, maxRows: 14 }" placeholder="评价项目成果质量，如数据准确性、报告完整性、图件规范性等" />
                <div class="form-tip">填写成果质量、交付完整性、技术审查等情况。</div>
              </el-form-item>
              <el-form-item label="全过程沟通情况">
                <el-input v-model="form.communicationNote" type="textarea" :autosize="{ minRows: 5, maxRows: 14 }" placeholder="描述项目全过程的内外部沟通情况，包括与业主的沟通、院内协调等" />
                <div class="form-tip">填写项目过程中与甲方、内部团队、上级部门的沟通情况。</div>
              </el-form-item>
            </el-form>
          </div>

          <!-- Bottom action bar -->
          <div class="action-bar">
            <span style="color:var(--pm-text-muted);font-size:13px">填写完成后请保存，自评内容将用于项目复盘与经验沉淀。</span>
            <div style="display:flex;gap:12px">
              <el-button @click="router.back()" size="large">返回项目</el-button>
              <el-button type="primary" @click="handleSave" :loading="saving" size="large">保存自评</el-button>
            </div>
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
