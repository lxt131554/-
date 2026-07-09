<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>新建项目</h2>
        <p style="color:var(--pm-text-secondary);margin-top:4px">按步骤填写项目立项、客户分析、策划启动和项目获取信息</p>
      </div>
      <div>
        <el-button @click="router.push('/projects')">返回列表</el-button>
      </div>
    </div>

    <div class="create-layout">
      <!-- Left: guide sidebar -->
      <aside class="create-aside">
        <div class="aside-card">
          <h3 class="aside-title">{{ stepTitle }}</h3>
          <p class="aside-desc">{{ stepDesc }}</p>
          <div class="aside-tip">必填项：项目名称</div>
        </div>
      </aside>

      <!-- Right: form area -->
      <div class="create-main">
        <div class="create-card">
          <!-- Steps indicator -->
          <el-steps :active="currentStep" align-center style="margin-bottom:32px">
            <el-step title="基本信息" />
            <el-step title="客户与前期分析" />
            <el-step title="策划启动" />
            <el-step title="项目获取" />
            <el-step title="确认创建" />
          </el-steps>

          <!-- STEP 1: 基本信息 -->
          <div v-show="currentStep === 0">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="140px">
              <el-form-item label="项目名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入项目名称" maxlength="255" />
              </el-form-item>
              <el-form-item label="项目描述">
                <el-input v-model="form.description" type="textarea" :autosize="{minRows:3,maxRows:8}" placeholder="选填，简要描述项目背景和主要内容" />
              </el-form-item>
            </el-form>
          </div>

          <!-- STEP 2: 客户与前期分析 -->
          <div v-show="currentStep === 1">
            <el-form :model="form" label-width="140px">
              <!-- 客户分析 -->
              <el-divider content-position="left"><strong>客户分析</strong></el-divider>
              <el-form-item label="客户等级/项目分级">
                <el-select v-model="form.customerLevel" placeholder="请选择项目分级" clearable style="width:100%">
                  <el-option label="S级（战略性项目）" value="S" />
                  <el-option label="A级（重大项目）" value="A" />
                  <el-option label="B级（成长型项目）" value="B" />
                  <el-option label="C级（一般项目）" value="C" />
                  <el-option label="D级（观察项目）" value="D" />
                </el-select>
              </el-form-item>
              <el-form-item label="双方联系人">
                <el-input v-model="form.contacts" type="textarea" :rows="2"
                  placeholder="例：甲方：张局长 138xxxx；乙方：李工 139xxxx" />
              </el-form-item>
              <el-form-item label="成果产出类型">
                <el-select v-model="form.achievementType" placeholder="请选择或直接输入" clearable filterable allow-create style="width:100%">
                  <el-option label="规划文本+图件+统计表" value="规划文本+图件+统计表" />
                  <el-option label="调查报告+数据库" value="调查报告+数据库" />
                  <el-option label="实施方案+图纸" value="实施方案+图纸" />
                  <el-option label="规划设计+预算" value="规划设计+预算" />
                  <el-option label="研究报告" value="研究报告" />
                  <el-option label="咨询报告" value="咨询报告" />
                </el-select>
              </el-form-item>
              <el-form-item label="审核审批要求">
                <el-input v-model="form.approvalRequirements" type="textarea" :rows="2"
                  placeholder="例：需通过省林业局审批、县政府常务会审查" />
              </el-form-item>
              <el-form-item label="项目重要性">
                <el-input v-model="form.projectImportance" type="textarea" :rows="2"
                  placeholder="例：对甲方是年度重点任务、对我院是拓展新业务领域的关键项目" />
              </el-form-item>
              <el-form-item label="成果方向附件">
                <el-input v-model="form.achievementDirection" type="textarea" :rows="2"
                  placeholder="例：规划文本送省厅备案、图件提交县自然资源局入库" />
              </el-form-item>

              <!-- 前期分析 -->
              <el-divider content-position="left"><strong>前期分析</strong></el-divider>
              <el-form-item label="能否承接判断">
                <el-input v-model="form.canUndertake" type="textarea" :rows="2"
                  placeholder="例：项目涉及专业齐全，院内有足够技术力量，可以承接" />
              </el-form-item>
              <el-form-item label="主要风险">
                <el-input v-model="form.mainRisks" type="textarea" :rows="2"
                  placeholder="例：地形复杂导致外业延期；地方协调存在不确定性" />
              </el-form-item>
              <el-form-item label="关键约束">
                <el-input v-model="form.keyConstraints" type="textarea" :rows="2"
                  placeholder="例：工期8个月；需跨部门协作" />
              </el-form-item>
              <el-form-item label="成果交付要求">
                <el-input v-model="form.deliverableRequirements" type="textarea" :rows="2"
                  placeholder="例：纸质报告10套、电子版5份" />
              </el-form-item>
              <el-form-item label="预计审批路径">
                <el-input v-model="form.approvalPath" type="textarea" :rows="2"
                  placeholder="例：院内审核→专家评审→县林业局审批" />
              </el-form-item>
            </el-form>
          </div>

          <!-- STEP 3: 策划启动 -->
          <div v-show="currentStep === 2">
            <el-form :model="form" label-width="140px">
              <el-form-item label="人力资源配置">
                <el-input v-model="form.hrAllocation" type="textarea" :rows="2"
                  placeholder="例：项目负责人1人、外业5人、内业3人" />
              </el-form-item>
              <el-form-item label="预计阶段成果">
                <el-input v-model="form.expectedOutputs" type="textarea" :rows="2"
                  placeholder="例：1.调查报告 2.规划文本 3.图件集" />
              </el-form-item>
              <el-form-item label="核心资料">
                <el-input v-model="form.coreMaterials" type="textarea" :rows="2"
                  placeholder="例：国土三调数据、林地一张图" />
              </el-form-item>
              <el-form-item label="项目组组建">
                <el-input v-model="form.teamSetup" type="textarea" :rows="2"
                  placeholder="例：以张主任为组长，15人，分4个专业组" />
              </el-form-item>
              <el-form-item label="核心策略">
                <el-input v-model="form.coreStrategy" type="textarea" :rows="2"
                  placeholder="例：分组并行作业；关键节点向院领导汇报" />
              </el-form-item>
            </el-form>
          </div>

          <!-- STEP 4: 项目获取 -->
          <div v-show="currentStep === 3">
            <el-form :model="form" label-width="140px">
              <el-form-item label="投标情况">
                <el-input v-model="form.bidSituation" type="textarea" :rows="2"
                  placeholder="例：竞争性谈判中标，参与单位3家" />
              </el-form-item>
              <el-form-item label="采购程序">
                <el-input v-model="form.procurementInfo" type="textarea" :rows="2"
                  placeholder="例：政府采购公开招标，咨询中心负责" />
              </el-form-item>
              <el-form-item label="获取结果">
                <el-input v-model="form.acquisitionResult" type="textarea" :rows="2"
                  placeholder="例：成功中标，合同额120万元" />
              </el-form-item>
            </el-form>
          </div>

          <!-- STEP 5: 确认创建 -->
          <div v-show="currentStep === 4">
            <div class="summary-section">
              <h4>基本信息 <el-button link size="small" @click="currentStep = 0">返回修改</el-button></h4>
              <p><b>项目名称：</b>{{ form.name || '未填写' }}</p>
              <p v-if="form.description"><b>项目描述：</b>{{ form.description.length > 100 ? form.description.substring(0, 100) + '...' : form.description }}</p>
            </div>
            <div class="summary-section">
              <h4>客户与前期分析 <el-button link size="small" @click="currentStep = 1">返回修改</el-button></h4>
              <p><b>项目分级：</b>{{ form.customerLevel || '未填写' }}</p>
              <p><b>成果产出类型：</b>{{ form.achievementType || '未填写' }}</p>
              <p><b>主要风险：</b>{{ form.mainRisks ? '已填写' : '未填写' }}</p>
              <p><b>关键约束：</b>{{ form.keyConstraints ? '已填写' : '未填写' }}</p>
            </div>
            <div class="summary-section">
              <h4>策划启动 <el-button link size="small" @click="currentStep = 2">返回修改</el-button></h4>
              <p><b>人力资源配置：</b>{{ form.hrAllocation ? '已填写' : '未填写' }}</p>
              <p><b>核心策略：</b>{{ form.coreStrategy ? '已填写' : '未填写' }}</p>
            </div>
            <div class="summary-section">
              <h4>项目获取 <el-button link size="small" @click="currentStep = 3">返回修改</el-button></h4>
              <p><b>投标情况：</b>{{ form.bidSituation ? '已填写' : '未填写' }}</p>
              <p><b>获取结果：</b>{{ form.acquisitionResult ? '已填写' : '未填写' }}</p>
            </div>
          </div>

          <!-- Bottom action bar -->
          <div class="form-action-bar" style="margin-top:24px">
            <span class="form-action-tip" style="font-size:13px;color:var(--pm-text-muted)">仅项目名称为必填，其余信息可根据实际情况补充</span>
            <div class="form-action-buttons" style="display:flex;gap:10px">
              <el-button @click="router.push('/projects')">取消</el-button>
              <el-button v-if="currentStep > 0" @click="currentStep--">上一步</el-button>
              <el-button v-if="currentStep < 4" type="primary" @click="nextStep">下一步</el-button>
              <el-button v-else type="primary" :loading="creating" @click="handleCreate">创建项目</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { createProject } from '../api/project'
import { ElMessage } from 'element-plus'
import { showActionError } from '../utils/actionGuards'

const router = useRouter()
const auth = useAuthStore()
const role = auth.user?.role

// Only manager or admin can create projects
if (role !== 'manager' && role !== 'admin') {
  router.replace('/projects')
}

const creating = ref(false)
const formRef = ref(null)
const currentStep = ref(0)

const form = reactive({
  name: '', description: '',
  customerLevel: '', contacts: '', achievementType: '', approvalRequirements: '',
  projectImportance: '', achievementDirection: '',
  canUndertake: '', mainRisks: '', keyConstraints: '', deliverableRequirements: '', approvalPath: '',
  hrAllocation: '', expectedOutputs: '', coreMaterials: '', teamSetup: '', coreStrategy: '',
  bidSituation: '', procurementInfo: '', acquisitionResult: ''
})

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

function nextStep() {
  if (currentStep.value === 0 && !form.name.trim()) {
    ElMessage.warning('请填写项目名称')
    return
  }
  if (currentStep.value < 4) {
    currentStep.value++
  }
}

const steps = [
  { title: '基本信息', desc: '先填写项目名称和简要描述，后续信息可按步骤逐步补充。' },
  { title: '客户与前期分析', desc: '用于记录客户背景、审批要求、承接判断、风险约束和交付要求。' },
  { title: '策划启动', desc: '用于明确项目团队、阶段成果、核心资料和执行策略。' },
  { title: '项目获取', desc: '用于记录投标、采购流程和最终获取结果。' },
  { title: '确认创建', desc: '请核对关键信息，确认无误后创建项目。' }
]
const stepTitle = computed(() => steps[currentStep.value].title)
const stepDesc = computed(() => steps[currentStep.value].desc)

async function handleCreate() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    await createProject({ ...form })
    ElMessage.success('项目创建成功')
    router.push('/projects')
  } catch (error) {
    showActionError(error, '项目创建失败')
  } finally { creating.value = false }
}
</script>

<style scoped>
.create-layout { display:grid; grid-template-columns:260px 1fr; gap:24px; align-items:start; }
.create-aside { position:sticky; top:24px; }
.aside-card { background:var(--pm-surface); border:1px solid var(--pm-border); border-radius:var(--pm-radius); padding:20px; }
.aside-title { font-size:16px; margin-bottom:8px; }
.aside-desc { font-size:13px; color:var(--pm-text-secondary); line-height:1.6; }
.aside-tip { margin-top:16px; padding-top:12px; border-top:1px solid var(--pm-border); font-size:13px; color:var(--pm-red-text); }
.create-main { min-width:0; }
.create-card { background:var(--pm-surface); border:1px solid var(--pm-border); border-radius:var(--pm-radius); padding:28px 32px; }
.summary-section { margin-bottom:20px; padding-bottom:16px; border-bottom:1px solid var(--pm-border-light); }
.summary-section h4 { font-size:15px; display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.summary-section p { margin:4px 0; font-size:14px; color:var(--pm-text-secondary); }
.form-action-bar { display:flex; justify-content:space-between; align-items:center; }
@media (max-width:900px) { .create-layout { grid-template-columns:1fr; } .create-aside { position:static; } }
</style>
