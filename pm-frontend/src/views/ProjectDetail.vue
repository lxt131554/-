<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="router.push('/projects')"><el-icon><ArrowLeft /></el-icon> 返回列表</el-button>
      <h2 style="margin-top:8px">{{ project.name }}</h2>
      <p style="color:var(--pm-text-secondary);margin-top:4px">{{ project.description || '暂无描述' }}</p>
      <div v-if="deviationSummary" style="margin-top:4px">
        <el-tag type="warning" size="small">整体偏差: {{ deviationSummary }}</el-tag>
      </div>
      <el-button v-if="project.status=='active' && (auth.user?.role=='manager'||auth.user?.role=='admin')"
        type="warning" size="small" @click="handleCompleteProject" style="margin-left:12px">
        完成项目
      </el-button>
      <el-button size="small" @click="handleExport" style="margin-left:8px">
        <el-icon><Download /></el-icon> 导出 Excel
      </el-button>
    </div>

    <!-- 项目概况摘要 -->
    <section class="page-summary-grid" style="margin-bottom:16px">
      <div class="summary-card summary-card--primary">
        <div class="summary-card-label">项目状态</div>
        <div class="summary-card-value" style="font-size:18px">{{ statusText }}</div>
      </div>
      <div class="summary-card summary-card--primary">
        <div class="summary-card-label">当前阶段</div>
        <div class="summary-card-value" style="font-size:18px">{{ currentStageName }}</div>
      </div>
      <div class="summary-card" :class="project.status==='completed'?'summary-card--success':'summary-card--primary'">
        <div class="summary-card-label">计划完成</div>
        <div class="summary-card-value" style="font-size:18px">{{ latestPlanEnd || '—' }}</div>
      </div>
      <div class="summary-card" :class="openDeviationsCount>0?'summary-card--danger':'summary-card--success'">
        <div class="summary-card-value">{{ openDeviationsCount }}</div>
        <div class="summary-card-label">未关闭偏差</div>
        <div class="summary-card-hint" v-if="openDeviationsCount>0">需要关注</div>
      </div>
      <div class="summary-card" :class="openSupportsCount>0?'summary-card--warning':'summary-card--success'">
        <div class="summary-card-value">{{ openSupportsCount }}</div>
        <div class="summary-card-label">支持事项</div>
      </div>
    </section>

    <!-- 启动与策划信息 -->
    <div class="card-box" style="margin-bottom:16px">
      <div class="page-toolbar">
        <span class="section-title">启动与策划信息</span>
        <el-button v-if="!planningEditMode && (auth.user?.role=='manager'||auth.user?.role=='admin')"
          size="small" @click="startPlanningEdit">
          <el-icon><Edit /></el-icon> {{ hasPlanningData ? '编辑' : '填写' }}
        </el-button>
        <template v-if="planningEditMode">
          <el-button size="small" @click="cancelPlanningEdit">取消</el-button>
          <el-button type="primary" size="small" @click="handleSavePlanning" :loading="savingPlanning">保存</el-button>
        </template>
      </div>

      <!-- View mode: grouped sections -->
      <template v-if="!planningEditMode && hasPlanningData">
        <section class="section-block" style="margin-bottom:12px">
          <h4 class="section-title" style="margin-top:0;margin-bottom:12px">客户与立项判断</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="客户等级">{{ project.customerLevel || '-' }}</el-descriptions-item>
            <el-descriptions-item label="成果产出类型">{{ project.achievementType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="双方联系人" :span="2">{{ project.contacts || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审核审批要求" :span="2">{{ project.approvalRequirements || '-' }}</el-descriptions-item>
            <el-descriptions-item label="项目重要性" :span="2">{{ project.projectImportance || '-' }}</el-descriptions-item>
            <el-descriptions-item label="成果方向及附件" :span="2">{{ project.achievementDirection || '-' }}</el-descriptions-item>
          </el-descriptions>
        </section>
        <section class="section-block" style="margin-bottom:12px">
          <h4 class="section-title" style="margin-top:0;margin-bottom:12px">前期分析与约束</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="能否承接判断" :span="2">{{ project.canUndertake || '-' }}</el-descriptions-item>
            <el-descriptions-item label="主要风险" :span="2">{{ project.mainRisks || '-' }}</el-descriptions-item>
            <el-descriptions-item label="关键约束" :span="2">{{ project.keyConstraints || '-' }}</el-descriptions-item>
            <el-descriptions-item label="成果交付要求" :span="2">{{ project.deliverableRequirements || '-' }}</el-descriptions-item>
            <el-descriptions-item label="预计审批路径" :span="2">{{ project.approvalPath || '-' }}</el-descriptions-item>
          </el-descriptions>
        </section>
        <section class="section-block" style="margin-bottom:12px">
          <h4 class="section-title" style="margin-top:0;margin-bottom:12px">策划与资源配置</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="人力资源配置" :span="2">{{ project.hrAllocation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="预计阶段成果" :span="2">{{ project.expectedOutputs || '-' }}</el-descriptions-item>
            <el-descriptions-item label="核心资料" :span="2">{{ project.coreMaterials || '-' }}</el-descriptions-item>
            <el-descriptions-item label="项目组组建" :span="2">{{ project.teamSetup || '-' }}</el-descriptions-item>
            <el-descriptions-item label="核心策略" :span="2">{{ project.coreStrategy || '-' }}</el-descriptions-item>
          </el-descriptions>
        </section>
        <section class="section-block" style="margin-bottom:12px">
          <h4 class="section-title" style="margin-top:0;margin-bottom:12px">项目获取与审批</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="投标情况" :span="2">{{ project.bidSituation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="采购程序" :span="2">{{ project.procurementInfo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="获取结果" :span="2">{{ project.acquisitionResult || '-' }}</el-descriptions-item>
          </el-descriptions>
        </section>
      </template>
      <el-empty v-else-if="!planningEditMode" description="暂未填写启动与策划信息" :image-size="80">
        <el-button v-if="auth.user?.role=='manager'||auth.user?.role=='admin'" type="primary" @click="startPlanningEdit">
          立即填写
        </el-button>
      </el-empty>

      <!-- Edit mode -->
      <div v-else class="edit-planning-section">
        <el-form :model="planningForm" label-width="130px">
          <el-divider content-position="left"><strong>客户分析</strong></el-divider>
          <el-form-item label="客户等级/项目分级">
            <el-select v-model="planningForm.customerLevel" placeholder="请选择项目分级" clearable style="width:100%">
              <el-option label="S级（战略性项目）" value="S" />
              <el-option label="A级（重大项目）" value="A" />
              <el-option label="B级（成长型项目）" value="B" />
              <el-option label="C级（一般项目）" value="C" />
              <el-option label="D级（观察项目）" value="D" />
            </el-select>
          </el-form-item>
          <el-form-item label="双方联系人">
            <el-input v-model="planningForm.contacts" type="textarea" :rows="2"
              placeholder="例：甲方：张局长 138xxxx；乙方：李工 139xxxx" />
          </el-form-item>
          <el-form-item label="成果产出类型">
            <el-select v-model="planningForm.achievementType" placeholder="请选择或直接输入" clearable filterable allow-create style="width:100%">
              <el-option label="规划文本+图件+统计表" value="规划文本+图件+统计表" />
              <el-option label="调查报告+数据库" value="调查报告+数据库" />
              <el-option label="实施方案+图纸" value="实施方案+图纸" />
              <el-option label="规划设计+预算" value="规划设计+预算" />
              <el-option label="研究报告" value="研究报告" />
              <el-option label="咨询报告" value="咨询报告" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核审批要求">
            <el-input v-model="planningForm.approvalRequirements" type="textarea" :rows="2"
              placeholder="例：需通过省林业局审批、县政府常务会审查" />
          </el-form-item>
          <el-form-item label="项目重要性">
            <el-input v-model="planningForm.projectImportance" type="textarea" :rows="2"
              placeholder="例：对甲方是年度重点任务、对我院是拓展新业务领域的关键项目" />
          </el-form-item>
          <el-form-item label="成果方向附件">
            <el-input v-model="planningForm.achievementDirection" type="textarea" :rows="2"
              placeholder="例：规划文本送省厅备案、图件提交县自然资源局入库" />
          </el-form-item>

          <el-divider content-position="left"><strong>前期分析</strong></el-divider>
          <el-form-item label="能否承接判断">
            <el-input v-model="planningForm.canUndertake" type="textarea" :rows="2"
              placeholder="例：项目涉及专业齐全，院内有足够技术力量，可以承接" />
          </el-form-item>
          <el-form-item label="主要风险">
            <el-input v-model="planningForm.mainRisks" type="textarea" :rows="2"
              placeholder="例：地形复杂导致外业延期；地方协调存在不确定性" />
          </el-form-item>
          <el-form-item label="关键约束">
            <el-input v-model="planningForm.keyConstraints" type="textarea" :rows="2"
              placeholder="例：工期8个月；需跨部门协作" />
          </el-form-item>
          <el-form-item label="成果交付要求">
            <el-input v-model="planningForm.deliverableRequirements" type="textarea" :rows="2"
              placeholder="例：纸质报告10套、电子版5份" />
          </el-form-item>
          <el-form-item label="预计审批路径">
            <el-input v-model="planningForm.approvalPath" type="textarea" :rows="2"
              placeholder="例：院内审核→专家评审→县林业局审批" />
          </el-form-item>

          <el-divider content-position="left"><strong>策划启动</strong></el-divider>
          <el-form-item label="人力资源配置">
            <el-input v-model="planningForm.hrAllocation" type="textarea" :rows="2"
              placeholder="例：项目负责人1人、外业5人、内业3人" />
          </el-form-item>
          <el-form-item label="预计阶段成果">
            <el-input v-model="planningForm.expectedOutputs" type="textarea" :rows="2"
              placeholder="例：1.调查报告 2.规划文本 3.图件集" />
          </el-form-item>
          <el-form-item label="核心资料">
            <el-input v-model="planningForm.coreMaterials" type="textarea" :rows="2"
              placeholder="例：国土三调数据、林地一张图" />
          </el-form-item>
          <el-form-item label="项目组组建">
            <el-input v-model="planningForm.teamSetup" type="textarea" :rows="2"
              placeholder="例：以张主任为组长，15人，分4个专业组" />
          </el-form-item>
          <el-form-item label="核心策略">
            <el-input v-model="planningForm.coreStrategy" type="textarea" :rows="2"
              placeholder="例：分组并行作业；关键节点向院领导汇报" />
          </el-form-item>

          <el-divider content-position="left"><strong>项目获取</strong></el-divider>
          <el-form-item label="投标情况">
            <el-input v-model="planningForm.bidSituation" type="textarea" :rows="2"
              placeholder="例：竞争性谈判中标，参与单位3家" />
          </el-form-item>
          <el-form-item label="采购程序">
            <el-input v-model="planningForm.procurementInfo" type="textarea" :rows="2"
              placeholder="例：政府采购公开招标，咨询中心负责" />
          </el-form-item>
          <el-form-item label="获取结果">
            <el-input v-model="planningForm.acquisitionResult" type="textarea" :rows="2"
              placeholder="例：成功中标，合同额120万元" />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="card-box" style="margin-bottom:16px">
      <div class="page-toolbar">
        <span class="section-title">项目阶段</span>
        <el-button type="primary" size="small" @click="showAddStage=true"
          v-if="(auth.user?.role==='manager'||auth.user?.role==='admin') && project.status!=='completed'">
          <el-icon><Plus /></el-icon> 添加阶段
        </el-button>
        <el-button v-if="!stages.length && (auth.user?.role=='manager'||auth.user?.role=='admin')"
          type="success" size="small" @click="openTemplateDialog" style="margin-left:8px">
          使用标准模板
        </el-button>
      </div>
      <el-table v-if="stages.length" :data="stages">
        <el-table-column prop="stageName" label="阶段名称" min-width="160">
          <template #default="{row}">
            <el-link type="primary" @click="router.push(`/stages/${row.id}`)">{{ row.stageName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="assigneeName" label="责任人" min-width="120" />
        <el-table-column label="计划时间" min-width="200">
          <template #default="{row}">{{ row.planStart || '-' }} 至 {{ row.planEnd || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="110">
          <template #default="{row}">
            <el-tag v-if="row.status==='pending'" type="info" size="small">待开始</el-tag>
            <el-tag v-else-if="row.status==='in_progress'" type="primary" size="small">进行中</el-tag>
            <el-tag v-else-if="row.status==='submitted'" type="warning" size="small">待审阅</el-tag>
            <el-tag v-else-if="row.status==='completed'" type="success" size="small">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="80" align="center">
          <template #default="{row}">
            <span v-if="row.latestReport?.progressRate">{{ row.latestReport.progressRate }}%</span>
            <span v-else style="color:var(--pm-text-muted)">—</span>
          </template>
        </el-table-column>
        <el-table-column label="最新填报" min-width="200" show-overflow-tooltip>
          <template #default="{row}">{{ row.latestReport?.content || '暂无' }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="180">
          <template #default="{row}">
            <el-button text type="primary" @click="router.push(`/my-tasks/${row.id}/report`)"
              v-if="auth.user?.role==='engineer' && row.status!=='completed'">填报</el-button>
            <el-button text type="danger" @click="handleDeleteStage(row)" v-if="(auth.user?.role=='manager'||auth.user?.role=='admin') && project.status!=='completed'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无项目阶段" />
    </div>

    <div class="section-block" style="margin-bottom:16px">
      <div class="page-toolbar">
        <span class="section-title">变更控制</span>
        <el-button type="primary" size="small" @click="showAddChange=true"
          v-if="(auth.user?.role=='manager'||auth.user?.role=='admin') && project.status!=='completed'">
          <el-icon><Plus /></el-icon> 新增变更
        </el-button>
      </div>
      <el-table :data="changes" border stripe v-if="changes.length">
        <el-table-column prop="content" label="变更内容" min-width="240" show-overflow-tooltip>
          <template #default="{row}">
            <el-link type="primary" @click="router.push(`/changes/${row.id}`)">{{ row.content }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="confirmTime" label="确认时间" min-width="120" />
        <el-table-column prop="impact" label="影响范围" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="90" align="center">
          <template #default="{row}">
            <el-tag :type="row.status=='confirmed'?'success':'warning'" size="small">
              {{ row.status=='confirmed'?'已确认':'待确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="170" align="center">
          <template #default="{row}">
            <el-button text type="primary" size="small" @click="router.push(`/changes/${row.id}`)">查看详情</el-button>
            <el-button v-if="row.status=='pending' && (auth.user?.role=='manager'||auth.user?.role=='admin')" text type="success" size="small"
              @click="handleConfirmChange(row)">确认变更</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无变更记录" :image-size="60" />
    </div>

    <!-- 新增变更对话框 -->
    <el-dialog v-model="showAddChange" title="新增变更" width="500px" :close-on-click-modal="false">
      <el-form :model="changeForm" label-width="100px">
        <el-form-item label="变更核心内容" required>
          <el-input v-model="changeForm.content" type="textarea" :rows="3" placeholder="描述变更的核心内容" />
        </el-form-item>
        <el-form-item label="确认时间">
          <el-date-picker v-model="changeForm.confirmTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="影响范围">
          <el-input v-model="changeForm.impact" type="textarea" :rows="2" placeholder="对工期、质量、成本等的影响" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleAddChange" :loading="addingChange">保存</el-button>
        <el-button @click="showAddChange=false">取消</el-button>
      </template>
    </el-dialog>

    <div class="section-block" style="margin-bottom:16px">
      <div class="page-toolbar">
        <span class="section-title">项目成员</span>
        <el-button type="primary" size="small" @click="showAddMember=true"
          v-if="isProjectManager || auth.user?.role=='admin'">
          <el-icon><Plus /></el-icon> 添加成员
        </el-button>
      </div>
      <div class="member-tags">
        <el-tag v-for="m in members" :key="m.id"
          :closable="isProjectManager || auth.user?.role=='admin'"
          :type="m.roleInProject==='manager'?'warning':'success'"
          @close="handleRemoveMember(m)" size="large">
          {{ m.realName }}（{{ m.roleInProject==='manager'?'负责人':'工程师' }}）
          <span v-if="m.status=='pending'" style="margin-left:4px;font-size:11px;opacity:0.7">（待确认）</span>
        </el-tag>
      </div>
    </div>

    <div class="section-block" v-if="project.status=='completed' && (auth.user?.role=='manager'||auth.user?.role=='admin')" style="margin-top:16px">
      <div class="page-toolbar">
        <span class="section-title">收尾复盘</span>
      </div>
      <div style="display:flex;gap:12px">
        <el-button type="primary" @click="router.push(`/projects/${projectId}/review`)">
          项目自评
        </el-button>
        <el-button type="primary" @click="router.push(`/projects/${projectId}/experience`)">
          经验总结
        </el-button>
        <el-button type="primary" @click="router.push(`/projects/${projectId}/approval`)">
          成果评审
        </el-button>
      </div>
    </div>

    <el-dialog v-model="showAddStage" title="添加阶段" width="500px" :close-on-click-modal="false">
      <el-form :model="stageForm" label-width="80px">
        <el-form-item label="阶段名称" required>
          <el-input v-model="stageForm.stageName" placeholder="如：外业调查" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="stageForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="责任人" required>
          <el-select v-model="stageForm.assigneeId" placeholder="选择责任人" style="width:100%">
            <el-option v-for="m in members" :key="m.userId" :label="m.realName" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始">
          <el-date-picker v-model="stageForm.planStart" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="计划结束">
          <el-date-picker v-model="stageForm.planEnd" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="stageForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleAddStage" :loading="addingStage">添加</el-button>
        <el-button @click="showAddStage=false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAddMember" title="添加成员" width="400px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="用户ID">
          <el-input v-model="newMember.userId" placeholder="输入用户ID" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="newMember.roleInProject" style="width:100%">
            <el-option label="项目负责人" value="manager" />
            <el-option label="工程师" value="engineer" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleAddMember" :loading="addingMember">添加</el-button>
        <el-button @click="showAddMember=false">取消</el-button>
      </template>
    </el-dialog>
    <!-- 标准模板预览弹窗 -->
    <el-dialog v-model="showTemplateDialog" title="标准阶段模板预览" width="700px" :close-on-click-modal="false">
      <el-alert title="请确认以下阶段信息，可修改后一次性创建" type="info" :closable="false" show-icon style="margin-bottom:16px" />
      <el-form label-width="100px">
        <div v-for="(t, i) in templateStages" :key="i" style="border:1px solid var(--pm-border);border-radius:8px;padding:16px;margin-bottom:12px">
          <div style="font-weight:600;margin-bottom:8px">阶段 {{ i + 1 }}</div>
          <el-form-item label="阶段名称"><el-input v-model="t.stageName" /></el-form-item>
          <el-form-item label="责任人" required>
            <el-select v-model="t.assigneeId" placeholder="选择责任人" style="width:100%">
              <el-option v-for="m in members" :key="m.userId" :label="m.realName" :value="m.userId" />
            </el-select>
          </el-form-item>
          <el-form-item label="描述"><el-input v-model="t.description" /></el-form-item>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="计划开始">
                <el-date-picker v-model="t.planStart" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划结束">
                <el-date-picker v-model="t.planEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showTemplateDialog=false">取消</el-button>
        <el-button type="primary" @click="handleApplyTemplate" :loading="applyingTemplate">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getProjectDetail, updateProject } from '../api/project'
import { getStages, addStage, deleteStage } from '../api/stage'
import { getProjectMembers, addProjectMember, removeProjectMember } from '../api/project'
import request from '../api/index'
import { ElMessage } from 'element-plus'
import { confirmDanger, showActionError } from '../utils/actionGuards'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const projectId = route.params.id

const project = ref({})
const stages = ref([])
const members = ref([])
const showAddStage = ref(false)
const showAddMember = ref(false)

const addingStage = ref(false)
const addingMember = ref(false)
const stageForm = reactive({
  stageName: '', description: '', assigneeId: null,
  planStart: '', planEnd: '', sortOrder: 0
})
const newMember = reactive({ userId: '', roleInProject: 'engineer' })

const changes = ref([])
const showAddChange = ref(false)
const addingChange = ref(false)
const changeForm = reactive({ content: '', confirmTime: '', impact: '' })

// Planning section - inline edit mode
const planningEditMode = ref(false)
const savingPlanning = ref(false)
const planningForm = reactive({
  customerLevel: '', contacts: '', achievementType: '', approvalRequirements: '',
  projectImportance: '', achievementDirection: '',
  canUndertake: '', mainRisks: '', keyConstraints: '', deliverableRequirements: '',
  approvalPath: '', hrAllocation: '', expectedOutputs: '', coreMaterials: '',
  teamSetup: '', coreStrategy: '', bidSituation: '', procurementInfo: '', acquisitionResult: ''
})

const hasPlanningData = computed(() => {
  return project.value.customerLevel || project.value.contacts || project.value.canUndertake
    || project.value.mainRisks || project.value.teamSetup || project.value.hrAllocation
    || project.value.projectImportance || project.value.achievementDirection
})

const isProjectManager = computed(() => {
  return members.value.some(m => m.userId === auth.user?.id && m.roleInProject === 'manager')
})

const deviationSummary = computed(() => {
  const stagesList = stages.value || []
  const parts = []
  stagesList.forEach(s => {
    if (s.planEnd && s.actualEnd) {
      const planEnd = new Date(s.planEnd)
      const actualEnd = new Date(s.actualEnd)
      const diffDays = Math.ceil((actualEnd - planEnd) / (1000 * 60 * 60 * 24))
      if (diffDays > 0) parts.push(`${s.stageName}+${diffDays}天`)
    }
  })
  return parts.length ? parts.join('，') : ''
})

const statusText = computed(() => project.value.status === 'active' ? '进行中' : '已完成')
const currentStageName = computed(() => {
  const stagesList = stages.value || []
  const active = stagesList.find(s => s.status === 'in_progress' || s.status === 'submitted')
  return active ? active.stageName : (stagesList.length ? stagesList[stagesList.length - 1].stageName : '未开始')
})
const latestPlanEnd = computed(() => {
  const stagesList = stages.value || []
  if (!stagesList.length) return null
  return stagesList[stagesList.length - 1].planEnd
})
const openDeviationsCount = computed(() => {
  return (project.value.deviations || []).filter(d => d.status === 'open').length
})
const openSupportsCount = computed(() => {
  const supports = project.value.supportItems || project.value.supports || []
  return supports.filter(s => s.status === 'pending' || s.status === 'open').length
})

function startPlanningEdit() {
  const fields = ['customerLevel','contacts','achievementType','approvalRequirements',
    'projectImportance','achievementDirection',
    'canUndertake','mainRisks','keyConstraints','deliverableRequirements',
    'approvalPath','hrAllocation','expectedOutputs','coreMaterials',
    'teamSetup','coreStrategy','bidSituation','procurementInfo','acquisitionResult']
  fields.forEach(f => { planningForm[f] = project.value[f] || '' })
  planningEditMode.value = true
}

function cancelPlanningEdit() {
  planningEditMode.value = false
}

async function handleSavePlanning() {
  savingPlanning.value = true
  try {
    await updateProject(projectId, { ...project.value, ...planningForm })
    ElMessage.success('启动信息已保存')
    planningEditMode.value = false
    loadProject()
  } catch (error) {
    showActionError(error, '启动信息保存失败')
  } finally { savingPlanning.value = false }
}

async function loadProject() {
  const res = await getProjectDetail(projectId)
  project.value = res.data
}
async function loadStages() {
  const res = await getStages(projectId)
  stages.value = res.data
}
async function loadMembers() {
  const res = await getProjectMembers(projectId)
  members.value = res.data
}

async function handleAddStage() {
  if (!stageForm.stageName || !stageForm.assigneeId) {
    ElMessage.warning('请填写阶段名称和责任人')
    return
  }
  addingStage.value = true
  try {
    await addStage(projectId, { ...stageForm })
    ElMessage.success('阶段添加成功')
    showAddStage.value = false
    Object.assign(stageForm, { stageName: '', description: '', assigneeId: null, planStart: '', planEnd: '', sortOrder: 0 })
    loadStages()
  } catch (error) {
    showActionError(error, '阶段添加失败')
  } finally { addingStage.value = false }
}
async function handleDeleteStage(row) {
  try {
    await confirmDanger(`确认删除阶段“${row.stageName || row.id}”？删除后相关填报也可能受到影响。`, '删除阶段')
    await deleteStage(projectId, row.id)
    ElMessage.success('删除成功')
    loadStages()
  } catch (error) {
    showActionError(error, '删除阶段失败')
  }
}
const showTemplateDialog = ref(false)
const applyingTemplate = ref(false)
const templateStages = reactive([
  { stageName: '外业调查', description: '实地勘测、数据采集、样地调查', planStart: '', planEnd: '', assigneeId: null },
  { stageName: '内业整理', description: '数据整理、图纸绘制、统计分析', planStart: '', planEnd: '', assigneeId: null },
  { stageName: '成果提交', description: '编制报告、制作图件、提交审核', planStart: '', planEnd: '', assigneeId: null }
])

function openTemplateDialog() {
  const today = new Date()
  const offsets = [[15, 30], [10, 25], [10, 20]]
  templateStages.forEach((t, i) => {
    const start = new Date(today)
    start.setDate(start.getDate() + offsets[i][0])
    const end = new Date(today)
    end.setDate(end.getDate() + offsets[i][0] + offsets[i][1])
    t.planStart = start.toISOString().slice(0, 10)
    t.planEnd = end.toISOString().slice(0, 10)
  })
  showTemplateDialog.value = true
}

async function handleApplyTemplate() {
  const missing = templateStages.some(t => !t.assigneeId)
  if (missing) { ElMessage.warning('请为每个阶段选择责任人'); return }
  applyingTemplate.value = true
  try {
    const res = await request.post(`/stages/template/${projectId}`, templateStages)
    ElMessage.success(`已创建 ${res.data.length} 个阶段`)
    showTemplateDialog.value = false
    loadStages()
  } catch (error) {
    showActionError(error, '标准模板创建失败')
  } finally { applyingTemplate.value = false }
}
async function handleAddMember() {
  if (!newMember.userId) { ElMessage.warning('请输入用户ID'); return }
  addingMember.value = true
  try {
    await addProjectMember(projectId, { ...newMember })
    ElMessage.success('成员添加成功')
    showAddMember.value = false
    newMember.userId = ''
    loadMembers()
  } catch (error) {
    showActionError(error, '成员添加失败')
  } finally { addingMember.value = false }
}
async function handleRemoveMember(m) {
  try {
    await confirmDanger(`确认将“${m.realName || m.userId}”移出项目成员？`, '移除成员')
    await removeProjectMember(projectId, m.id)
    ElMessage.success('移除成功')
    loadMembers()
  } catch (error) {
    showActionError(error, '移除成员失败')
  }
}

async function loadChanges() {
  try {
    const res = await request.get(`/projects/${projectId}/changes`)
    changes.value = res.data || []
  } catch (error) {
    showActionError(error, '变更记录加载失败')
  }
}
async function handleAddChange() {
  if (!changeForm.content) { ElMessage.warning('请填写变更内容'); return }
  addingChange.value = true
  try {
    await request.post(`/projects/${projectId}/changes`, { ...changeForm })
    ElMessage.success('变更记录已保存')
    showAddChange.value = false
    Object.assign(changeForm, { content: '', confirmTime: '', impact: '' })
    loadChanges()
  } catch (error) {
    showActionError(error, '变更记录保存失败')
  } finally { addingChange.value = false }
}
async function handleConfirmChange(row) {
  try {
    await confirmDanger('确认该项目变更已完成审核，可以进入后续执行？', '确认变更')
    await request.put(`/changes/${row.id}/confirm`)
    ElMessage.success('变更已确认')
    loadChanges()
  } catch (error) {
    showActionError(error, '确认变更失败')
  }
}

async function handleCompleteProject() {
  try {
    await confirmDanger('确认将项目标记为已完成？完成后将进入收尾复盘相关流程。', '完成项目')
    await updateProject(projectId, { ...project.value, status: 'completed' })
    ElMessage.success('项目已完成')
    loadProject()
  } catch (error) {
    showActionError(error, '完成项目失败')
  }
}

function handleExport() {
  window.open(`/api/projects/${projectId}/export`, '_blank')
}

onMounted(() => { loadProject(); loadStages(); loadMembers(); loadChanges() })
</script>

<style scoped>
.section-title {
  font-weight: 600;
  font-size: 16px;
  color: var(--pm-text);
  letter-spacing: -0.01em;
}

.member-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.edit-planning-section {
  margin-top: 16px;
}

.edit-planning-section .el-divider {
  margin: 20px 0 16px;
}

.edit-planning-section .el-divider:first-child {
  margin-top: 0;
}

.edit-planning-section .el-form-item {
  margin-bottom: 16px;
}

/* Summary card color accent variants */
.summary-card--primary {
  border-left: 3px solid var(--pm-accent);
}
.summary-card--primary .summary-card-value {
  color: var(--pm-accent);
}

.summary-card--warning {
  border-left: 3px solid #F59E0B;
}
.summary-card--warning .summary-card-value {
  color: #F59E0B;
}

.summary-card--danger {
  border-left: 3px solid var(--pm-red-text);
}
.summary-card--danger .summary-card-value {
  color: var(--pm-red-text);
}

.summary-card--success {
  border-left: 3px solid var(--pm-green-text);
}
.summary-card--success .summary-card-value {
  color: var(--pm-green-text);
}
</style>
