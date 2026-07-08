<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.push('/projects')"><el-icon><ArrowLeft /></el-icon> 返回列表</el-button>
      <h2>新建项目</h2>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="130px">
      <!-- 基本信息 -->
      <div class="card-box" style="margin-bottom:16px">
        <div style="font-weight:600;font-size:16px;margin-bottom:16px">基本信息</div>
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述（选填）" />
        </el-form-item>
      </div>

      <!-- 客户分析 -->
      <div class="card-box" style="margin-bottom:16px">
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
      </div>

      <!-- 前期分析 -->
      <div class="card-box" style="margin-bottom:16px">
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
      </div>

      <!-- 策划启动 -->
      <div class="card-box" style="margin-bottom:16px">
        <el-divider content-position="left"><strong>策划启动</strong></el-divider>
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
      </div>

      <!-- 项目获取 -->
      <div class="card-box" style="margin-bottom:16px">
        <el-divider content-position="left"><strong>项目获取</strong></el-divider>
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
      </div>

      <div style="display:flex;gap:12px;justify-content:center;padding:24px 0">
        <el-button type="primary" size="large" @click="handleCreate" :loading="creating">创建项目</el-button>
        <el-button size="large" @click="router.push('/projects')">取消</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
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
.card-box .el-divider {
  margin: 0 0 16px;
}
</style>
