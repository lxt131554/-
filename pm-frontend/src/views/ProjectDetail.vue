<template>
  <div class="ws-page" v-loading="loading">
    <!-- ===== 1. TOP SUMMARY BAR ===== -->
    <div class="ws-topbar">
      <div class="ws-topbar-left">
        <h2 class="ws-project-name">{{ project.name || '项目详情' }}</h2>
        <el-tag :type="project.status === 'active' ? 'success' : 'info'" size="small">{{ statusText }}</el-tag>
        <span class="ws-meta-sep">|</span>
        <span class="ws-meta"><b>负责人</b>{{ managerNames || '未分配' }}</span>
        <span class="ws-meta"><b>当前阶段</b>{{ currentStageName }}</span>
        <span class="ws-meta"><b>计划完成</b>{{ latestPlanEnd || '未设置' }}</span>
        <template v-if="openDeviationsCount > 0 || openSupportsCount > 0">
          <span class="ws-meta-sep">|</span>
          <span v-if="openDeviationsCount > 0" class="ws-meta ws-meta--warn">未关闭偏差: {{ openDeviationsCount }}</span>
          <span v-if="openSupportsCount > 0" class="ws-meta ws-meta--warn">待处理支持: {{ openSupportsCount }}</span>
        </template>
        <span v-if="deviationSummary" class="ws-meta ws-meta--warn">{{ deviationSummary }}</span>
      </div>
      <div class="ws-topbar-right">
        <el-button size="small" @click="handleExport"><el-icon><Download /></el-icon>导出Excel</el-button>
        <el-button
          v-if="project.status!=='completed' && (auth.user?.role==='manager'||auth.user?.role==='admin')"
          size="small" @click="handleEditProject">编辑项目</el-button>
        <el-button v-if="project.status=='completed' && auth.user?.role=='admin'"
          size="small" @click="handleReopenProject">重新打开</el-button>
        <el-button v-if="project.status=='active' && (auth.user?.role=='manager'||auth.user?.role=='admin')"
          type="primary" size="small" @click="handleCompleteProject">完成项目</el-button>
        <el-button size="small" @click="router.push('/projects')"><el-icon><ArrowLeft /></el-icon>返回列表</el-button>
      </div>
    </div>

    <!-- ===== 2. TAB NAVIGATION ===== -->
    <el-tabs v-model="activeTab" type="border-card" class="ws-tabs">
      <!-- ===== Tab 1: 项目概览 ===== -->
      <el-tab-pane label="项目概览" name="overview">
        <div class="ws-overview">
          <!-- Left 70% -->
          <div class="ws-overview-main">
            <!-- 项目描述 -->
            <p class="ws-desc" v-if="project.description">{{ project.description }}</p>

            <!-- Manager/Admin: 下一步动作 -->
            <div v-if="(auth.user?.role==='manager'||auth.user?.role==='admin') && project.status!=='completed'" class="ws-section">
              <h3 class="ws-section-title">下一步动作</h3>
              <div class="ws-action-list">
                <div v-if="stages.some(s=>s.status==='submitted')" class="ws-action-item ws-action-item--warn">
                  <el-icon><Warning /></el-icon>
                  <span>有 {{ stages.filter(s=>s.status==='submitted').length }} 个阶段待审阅，请及时处理</span>
                </div>
                <div v-if="stages.some(s=>s.status==='pending')" class="ws-action-item">
                  <el-icon><Clock /></el-icon>
                  <span>有 {{ stages.filter(s=>s.status==='pending').length }} 个阶段待启动</span>
                </div>
                <div v-if="openDeviationsCount>0" class="ws-action-item ws-action-item--danger">
                  <el-icon><CircleCloseFilled /></el-icon>
                  <span>{{ openDeviationsCount }} 项偏差未关闭，请及时处理</span>
                </div>
                <div v-if="members.some(m=>m.status==='pending')" class="ws-action-item">
                  <el-icon><User /></el-icon>
                  <span>{{ members.filter(m=>m.status==='pending').length }} 位成员待确认邀请</span>
                </div>
              </div>
            </div>

            <!-- Engineer: 我的任务 -->
            <div v-if="auth.user?.role==='engineer' && project.status!=='completed'" class="ws-section">
              <h3 class="ws-section-title">我的任务</h3>
              <div v-if="assignedStages.length" class="ws-assigned-list">
                <div v-for="s in assignedStages" :key="s.id" class="ws-assigned-item">
                  <div class="ws-assigned-info">
                    <span class="ws-assigned-name">{{ s.stageName }}</span>
                    <el-tag v-if="s.status==='pending'" type="info" size="small">待开始</el-tag>
                    <el-tag v-else-if="s.status==='in_progress'" type="primary" size="small">进行中</el-tag>
                    <el-tag v-else-if="s.status==='submitted'" type="warning" size="small">待审阅</el-tag>
                    <el-tag v-else-if="s.status==='completed'" type="success" size="small">已完成</el-tag>
                  </div>
                  <div class="ws-assigned-progress">
                    <el-progress :percentage="s.latestReport?.progressRate || 0" :stroke-width="6" style="flex:1;max-width:200px" />
                    <span style="font-size:13px;color:var(--pm-text-muted);margin-left:8px">{{ s.latestReport?.progressRate || 0 }}%</span>
                  </div>
                  <el-button v-if="s.status!=='completed'" size="small" type="primary" link @click="router.push(`/my-tasks/${s.id}/report`)">去填报</el-button>
                </div>
              </div>
              <el-empty v-else description="暂无分配给你的阶段" :image-size="50" />
            </div>

            <!-- Leader: 风险关注 -->
            <div v-if="auth.user?.role==='leader'" class="ws-section">
              <h3 class="ws-section-title">风险关注</h3>
              <div class="ws-action-list">
                <div v-if="openDeviationsCount>0" class="ws-action-item ws-action-item--danger">
                  <el-icon><CircleCloseFilled /></el-icon>
                  <span>{{ openDeviationsCount }} 项偏差未关闭</span>
                </div>
                <div v-if="openSupportsCount>0" class="ws-action-item ws-action-item--warn">
                  <el-icon><Warning /></el-icon>
                  <span>{{ openSupportsCount }} 项支持事项待处理</span>
                </div>
                <div v-if="!openDeviationsCount && !openSupportsCount" class="ws-action-item ws-action-item--ok">
                  <el-icon><CircleCheck /></el-icon>
                  <span>项目运行正常，暂无风险事项</span>
                </div>
              </div>
            </div>

            <!-- 阶段进度 -->
            <div class="ws-section">
              <h3 class="ws-section-title">阶段进度</h3>
              <div v-if="stages.length" class="ws-stage-list">
                <div v-for="s in stages" :key="s.id" class="ws-stage-item" @click="router.push(`/stages/${s.id}`)" style="cursor:pointer">
                  <div class="ws-stage-item-top">
                    <span class="ws-stage-item-name">{{ s.stageName }}</span>
                    <el-tag v-if="s.status==='pending'" type="info" size="small">待开始</el-tag>
                    <el-tag v-else-if="s.status==='in_progress'" type="primary" size="small">进行中</el-tag>
                    <el-tag v-else-if="s.status==='submitted'" type="warning" size="small">待审阅</el-tag>
                    <el-tag v-else-if="s.status==='completed'" type="success" size="small">已完成</el-tag>
                    <span class="ws-stage-item-pct">{{ s.latestReport?.progressRate || 0 }}%</span>
                  </div>
                  <el-progress :percentage="s.latestReport?.progressRate || 0" :stroke-width="6" :show-text="false" />
                  <div class="ws-stage-item-meta" v-if="s.assigneeName">
                    <span>{{ s.assigneeName }}</span>
                    <span v-if="s.planEnd">{{ s.planStart || '—' }} ~ {{ s.planEnd }}</span>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无项目阶段" :image-size="50" />
            </div>

            <!-- 近期填报 -->
            <div class="ws-section" v-if="recentReports.length">
              <h3 class="ws-section-title">近期填报</h3>
              <div class="ws-report-list">
                <div v-for="(r, i) in recentReports" :key="i" class="ws-report-item">
                  <div class="ws-report-item-hd">
                    <span class="ws-report-item-stage">{{ r.stageName }}</span>
                    <span class="ws-report-item-time" v-if="r.time">{{ r.time }}</span>
                    <el-tag size="small" type="primary" effect="plain">{{ r.progressRate }}%</el-tag>
                  </div>
                  <div class="ws-report-item-body">{{ r.content }}</div>
                </div>
              </div>
            </div>

            <!-- 关键节点 -->
            <div class="ws-section" v-if="upcomingDeadlines.length">
              <h3 class="ws-section-title">关键节点（未来30天）</h3>
              <div class="ws-deadline-list">
                <div v-for="d in upcomingDeadlines" :key="d.id" class="ws-deadline-item" :class="{ 'ws-deadline-item--urgent': d.daysLeft <= 7 }">
                  <span class="ws-deadline-name">{{ d.stageName }}</span>
                  <span class="ws-deadline-date">{{ d.planEnd }}</span>
                  <span class="ws-deadline-days" :class="{ 'ws-deadline-days--urgent': d.daysLeft <= 7 }">剩余 {{ d.daysLeft }} 天</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Right 30% -->
          <div class="ws-overview-side">
            <!-- 项目团队 -->
            <div class="ws-side-card">
              <div class="ws-side-card-title">项目团队</div>
              <div class="ws-side-members">
                <div v-for="m in members" :key="m.id" class="ws-side-member">
                  <span class="ws-side-member-name">{{ m.realName }}</span>
                  <span class="ws-side-member-role">{{ m.roleInProject==='manager'?'负责人':'工程师' }}</span>
                </div>
              </div>
              <el-empty v-if="!members.length" description="暂无成员" :image-size="40" />
            </div>

            <!-- 偏差摘要 -->
            <div class="ws-side-card" @click="activeTab='process'" style="cursor:pointer">
              <div class="ws-side-card-title">
                偏差台账
                <span class="ws-side-card-link">详情 ></span>
              </div>
              <div class="ws-side-stat" :class="openDeviationsCount>0?'ws-side-stat--warn':'ws-side-stat--ok'">
                <span class="ws-side-stat-num">{{ openDeviationsCount }}</span>
                <span class="ws-side-stat-label">未关闭偏差</span>
              </div>
            </div>

            <!-- 支持事项摘要 -->
            <div class="ws-side-card" @click="activeTab='process'" style="cursor:pointer">
              <div class="ws-side-card-title">
                支持事项
                <span class="ws-side-card-link">详情 ></span>
              </div>
              <div class="ws-side-stat" :class="openSupportsCount>0?'ws-side-stat--warn':'ws-side-stat--ok'">
                <span class="ws-side-stat-num">{{ openSupportsCount }}</span>
                <span class="ws-side-stat-label">待处理</span>
              </div>
            </div>

            <!-- 收尾资料完整度 -->
            <div class="ws-side-card" @click="activeTab='closeout'" style="cursor:pointer">
              <div class="ws-side-card-title">
                收尾资料
                <span class="ws-side-card-link">详情 ></span>
              </div>
              <div class="ws-side-closeout">
                <span class="ws-side-closeout-frac">{{ closeoutCompleted }}/3</span>
                <el-progress :percentage="closeoutCompleted ? Math.round(closeoutCompleted/3*100) : 0" :stroke-width="6" style="flex:1" />
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ===== Tab 2: 启动策划 ===== -->
      <el-tab-pane label="启动策划" name="planning">
        <div class="ws-tab-toolbar">
          <span class="ws-tab-toolbar-title">启动与策划信息</span>
          <div>
            <el-button v-if="!planningEditMode && (auth.user?.role=='manager'||auth.user?.role=='admin') && project.status !== 'completed'"
              size="small" @click="startPlanningEdit">
              <el-icon><Edit /></el-icon> {{ hasPlanningData ? '编辑' : '填写' }}
            </el-button>
            <template v-if="planningEditMode">
              <el-button size="small" @click="cancelPlanningEdit">取消</el-button>
              <el-button type="primary" size="small" @click="handleSavePlanning" :loading="savingPlanning">保存</el-button>
            </template>
          </div>
        </div>

        <!-- View mode: spacious two-column layout -->
        <template v-if="!planningEditMode && hasPlanningData">
          <!-- 客户与立项判断 -->
          <div style="border-bottom:1px solid var(--pm-border);padding-bottom:20px;margin-bottom:20px">
            <h4 style="font-weight:600;font-size:15px;color:var(--pm-text);margin:0 0 16px 0">客户与立项判断</h4>
            <div style="display:grid;grid-template-columns:140px 1fr 140px 1fr;gap:12px 20px;align-items:baseline">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px">客户等级</div>
              <div>{{ project.customerLevel || '—' }}</div>
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px">成果产出类型</div>
              <div>{{ project.achievementType || '—' }}</div>
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px">双方联系人</div>
              <div>{{ project.contacts || '—' }}</div>
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px">审核审批要求</div>
              <div>{{ project.approvalRequirements || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">项目重要性</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.projectImportance || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">成果方向及附件</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.achievementDirection || '—' }}</div>
            </div>
          </div>
          <!-- 前期分析与约束 -->
          <div style="border-bottom:1px solid var(--pm-border);padding-bottom:20px;margin-bottom:20px">
            <h4 style="font-weight:600;font-size:15px;color:var(--pm-text);margin:0 0 16px 0">前期分析与约束</h4>
            <div>
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">能否承接判断</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.canUndertake || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">主要风险</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.mainRisks || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">关键约束</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.keyConstraints || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">成果交付要求</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.deliverableRequirements || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">预计审批路径</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.approvalPath || '—' }}</div>
            </div>
          </div>
          <!-- 策划与资源配置 -->
          <div style="border-bottom:1px solid var(--pm-border);padding-bottom:20px;margin-bottom:20px">
            <h4 style="font-weight:600;font-size:15px;color:var(--pm-text);margin:0 0 16px 0">策划与资源配置</h4>
            <div>
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">人力资源配置</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.hrAllocation || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">预计阶段成果</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.expectedOutputs || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">核心资料</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.coreMaterials || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">项目组组建</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.teamSetup || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">核心策略</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.coreStrategy || '—' }}</div>
            </div>
          </div>
          <!-- 项目获取与审批 -->
          <div>
            <h4 style="font-weight:600;font-size:15px;color:var(--pm-text);margin:0 0 16px 0">项目获取与审批</h4>
            <div>
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">投标情况</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.bidSituation || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">采购程序</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.procurementInfo || '—' }}</div>
            </div>
            <div style="margin-top:16px">
              <div style="font-weight:500;color:var(--pm-text-secondary);font-size:14px;margin-bottom:4px">获取结果</div>
              <div style="line-height:1.7;white-space:pre-wrap">{{ project.acquisitionResult || '—' }}</div>
            </div>
          </div>
        </template>
        <el-empty v-else-if="!planningEditMode" description="暂未填写启动与策划信息" :image-size="80">
          <el-button v-if="(auth.user?.role=='manager'||auth.user?.role=='admin') && project.status !== 'completed'" type="primary" @click="startPlanningEdit">
            立即填写
          </el-button>
        </el-empty>

        <!-- Edit mode -->
        <div v-if="planningEditMode" class="edit-planning-section" style="margin-top:16px">
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
      </el-tab-pane>

      <!-- ===== Tab 3: 阶段任务 ===== -->
      <el-tab-pane label="阶段任务" name="stages">
        <div class="ws-tab-toolbar">
          <span class="ws-tab-toolbar-title">项目阶段</span>
          <div>
            <el-button type="primary" size="small" @click="openAddStage"
              v-if="(auth.user?.role==='manager'||auth.user?.role==='admin') && project.status!=='completed'">
              <el-icon><Plus /></el-icon> 添加阶段
            </el-button>
            <el-button v-if="!stages.length && (auth.user?.role=='manager'||auth.user?.role=='admin') && project.status !== 'completed'"
              type="success" size="small" @click="openTemplateDialog" style="margin-left:8px">
              使用标准模板
            </el-button>
          </div>
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
          <el-table-column label="操作" width="160" fixed="right" align="center">
            <template #default="{row}">
              <div style="display:flex;align-items:center;justify-content:center;gap:12px">
                <el-button text type="primary" size="small" @click="router.push(`/my-tasks/${row.id}/report`)"
                  v-if="(auth.user?.id===row.assigneeId || auth.user?.role==='engineer' || auth.user?.role=='manager' || auth.user?.role=='admin') && row.status!=='completed'">填报</el-button>
                <el-button text type="danger" size="small" @click="handleDeleteStage(row)" v-if="(auth.user?.role=='manager'||auth.user?.role=='admin') && project.status!=='completed'">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无项目阶段" />
      </el-tab-pane>

      <!-- ===== Tab 4: 成员分工 ===== -->
      <el-tab-pane label="成员分工" name="members">
        <div class="ws-tab-toolbar">
          <span class="ws-tab-toolbar-title">项目成员</span>
          <el-button type="primary" size="small" @click="showMemberManage=true"
            v-if="(isProjectManager || auth.user?.role=='admin') && project.status !== 'completed'">
            <el-icon><Setting /></el-icon> 成员管理
          </el-button>
        </div>
        <div class="member-tags">
          <el-tag v-for="m in members" :key="m.id"
            :type="m.roleInProject==='manager'?'warning':'success'" size="large">
            {{ m.realName }}（{{ m.roleInProject==='manager'?'负责人':'工程师' }}）
            <span v-if="m.status=='pending'" style="margin-left:4px;font-size:11px;opacity:0.7">（待确认）</span>
          </el-tag>
        </div>
        <el-empty v-if="!members.length" description="暂无项目成员" :image-size="60" />
      </el-tab-pane>

      <!-- ===== Tab 5: 过程管控 ===== -->
      <el-tab-pane label="过程管控" name="process">
        <!-- Section: 偏差台账 -->
        <div class="ws-section">
          <div class="ws-section-header">
            <h3 class="ws-section-title">偏差台账</h3>
            <el-button text type="primary" size="small" @click="router.push('/deviations')">查看全部</el-button>
          </div>
          <el-table v-if="project.deviations && project.deviations.length" :data="showAllDeviations ? project.deviations : project.deviations.slice(0, 3)" stripe size="small">
            <el-table-column label="描述" min-width="200" show-overflow-tooltip>
              <template #default="{row}">{{ row.description || row.content || row.title || '—' }}</template>
            </el-table-column>
            <el-table-column prop="stageName" label="关联阶段" min-width="120" />
            <el-table-column prop="createTime" label="创建时间" min-width="140" />
            <el-table-column label="状态" min-width="90" align="center">
              <template #default="{row}">
                <el-tag :type="row.status==='open'?'danger':'success'" size="small">{{ row.status==='open'?'未关闭':'已关闭' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{row}">
                <el-button text type="primary" size="small" @click="router.push(`/deviations/${row.id}`)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无偏差记录" :image-size="50" />
          <div v-if="project.deviations && project.deviations.length > 3" style="text-align:center;margin-top:8px">
            <el-button text type="primary" size="small" @click="showAllDeviations=!showAllDeviations">
              {{ showAllDeviations ? '收起' : `展开全部 (${project.deviations.length})` }}
            </el-button>
          </div>
        </div>

        <!-- Section: 支持事项 -->
        <div class="ws-section">
          <div class="ws-section-header">
            <h3 class="ws-section-title">支持事项</h3>
            <el-button text type="primary" size="small" @click="router.push('/supports')">查看全部</el-button>
          </div>
          <el-table v-if="projectSupports.length" :data="showAllSupports ? projectSupports : projectSupports.slice(0, 3)" stripe size="small">
            <el-table-column label="标题" min-width="180" show-overflow-tooltip>
              <template #default="{row}">{{ row.title || row.content || row.description || '—' }}</template>
            </el-table-column>
            <el-table-column prop="applicantName" label="申请人" min-width="100" />
            <el-table-column prop="createTime" label="创建时间" min-width="140" />
            <el-table-column label="状态" min-width="90" align="center">
              <template #default="{row}">
                <el-tag :type="row.status==='pending'||row.status==='open'?'warning':(row.status==='resolved'?'success':'info')" size="small">
                  {{ row.status==='pending'?'待处理':(row.status==='open'?'处理中':(row.status==='resolved'?'已解决':row.status)) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{row}">
                <el-button text type="primary" size="small" @click="router.push(`/supports/${row.id}`)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无支持事项" :image-size="50" />
          <div v-if="projectSupports.length > 3" style="text-align:center;margin-top:8px">
            <el-button text type="primary" size="small" @click="showAllSupports=!showAllSupports">
              {{ showAllSupports ? '收起' : `展开全部 (${projectSupports.length})` }}
            </el-button>
          </div>
        </div>

        <!-- Section: 项目变更 -->
        <div class="ws-section">
          <div class="ws-section-header">
            <h3 class="ws-section-title">项目变更</h3>
            <el-button type="primary" size="small" @click="showAddChange=true"
              v-if="(auth.user?.role=='manager'||auth.user?.role=='admin') && project.status!=='completed'">
              <el-icon><Plus /></el-icon> 新增变更
            </el-button>
          </div>
          <el-table :data="changes" stripe v-if="changes.length">
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
            <el-table-column label="操作" width="170" fixed="right" align="center">
              <template #default="{row}">
                <el-button text type="primary" size="small" @click="router.push(`/changes/${row.id}`)">查看详情</el-button>
                <el-button v-if="row.status=='pending' && (auth.user?.role=='manager'||auth.user?.role=='admin')" text type="success" size="small"
                  @click="handleConfirmChange(row)">确认变更</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无变更记录" :image-size="60" />
        </div>
      </el-tab-pane>

      <!-- ===== Tab 6: 收尾资料 ===== -->
      <el-tab-pane label="收尾资料" name="closeout">
        <div class="ws-tab-toolbar">
          <span class="ws-tab-toolbar-title">收尾资料</span>
        </div>
        <!-- Manager/Admin view -->
        <template v-if="auth.user?.role=='manager'||auth.user?.role=='admin'">
          <div style="display:flex;align-items:center;gap:16px;margin-bottom:20px;font-size:14px">
            <span style="font-weight:500;white-space:nowrap">收尾资料完整度</span>
            <span style="font-weight:600;font-size:18px">{{ closeoutCompleted }}/3</span>
            <el-progress :percentage="closeoutCompleted ? Math.round(closeoutCompleted/3*100) : 0" :stroke-width="8" style="flex:1;max-width:480px;min-width:200px" />
          </div>
          <el-table :data="closeoutItems" stripe style="width:100%">
            <el-table-column prop="label" label="内容" min-width="180" />
            <el-table-column prop="status" label="状态" min-width="120" align="center">
              <template #default="{row}">
                <el-tag v-if="row.status==='已完成'" type="success" size="small">已完成</el-tag>
                <el-tag v-else-if="row.status==='未填写'" type="info" size="small">未填写</el-tag>
                <el-tag v-else size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{row}">
                <el-button size="small" type="primary" link @click="router.push(row.url)">{{ row.status==='已完成' ? '查看/编辑' : '去填写' }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>
        <!-- Leader/Engineer view -->
        <template v-else>
          <div style="font-size:14px;color:var(--pm-text-secondary);margin-bottom:16px">完整度：{{ closeoutCompleted }}/3</div>
          <el-table :data="closeoutItems" stripe style="width:100%">
            <el-table-column prop="label" label="内容" min-width="180" />
            <el-table-column prop="status" label="状态" min-width="120" align="center">
              <template #default="{row}">
                <el-tag v-if="row.status==='已完成'" type="success" size="small">已完成</el-tag>
                <el-tag v-else type="info" size="small">未填写</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- ===== 3. ALL DIALOGS ===== -->

    <!-- 新增变更对话框 -->
    <el-dialog v-model="showAddChange" title="新增变更" width="500px" :close-on-click-modal="false" append-to-body align-center :lock-scroll="true">
      <el-form :model="changeForm" label-width="120px">
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

    <!-- 添加阶段对话框 -->
    <el-dialog v-model="showAddStage" title="添加阶段" width="560px" :close-on-click-modal="false" append-to-body align-center :lock-scroll="true">
      <el-form :model="stageForm" label-width="120px">
        <el-form-item label="从模板选择" style="margin-bottom:12px">
          <el-select v-model="selectedTemplate" placeholder="选择或输入阶段名称" clearable filterable allow-create style="width:100%" @change="onTemplateSelect">
            <el-option v-for="t in templateStages" :key="t.stageName" :label="t.stageName" :value="t.stageName" />
          </el-select>
        </el-form-item>
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

    <!-- 添加成员对话框 -->
    <el-dialog v-model="showAddMember" title="添加成员" width="500px" :close-on-click-modal="false" append-to-body align-center :lock-scroll="true" @open="loadAvailableUsers">
      <el-form label-width="120px">
        <el-form-item label="选择人员">
          <el-select v-model="newMember.userId" placeholder="搜索姓名或用户名" filterable style="width:100%">
            <el-option v-for="u in availableUsers" :key="u.id" :label="`${u.realName}（${u.username}）- ${u.dept || '无部门'}`" :value="u.id">
              <div style="display:flex;justify-content:space-between;align-items:center">
                <span>{{ u.realName }}</span>
                <span style="color:var(--pm-text-muted);font-size:12px">{{ u.username }} · {{ u.dept || '—' }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目角色">
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

    <!-- 成员管理对话框 -->
    <el-dialog v-model="showMemberManage" title="成员管理" width="750px" :close-on-click-modal="false" append-to-body align-center :lock-scroll="true">
      <div style="margin-bottom:12px">
        <el-button type="primary" size="small" @click="showAddMember=true"
          v-if="project.status !== 'completed'">
          <el-icon><Plus /></el-icon> 添加成员
        </el-button>
      </div>
      <el-table :data="members" border stripe>
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column label="部门" min-width="120">
          <template #default="{row}">{{ row.dept || '—' }}</template>
        </el-table-column>
        <el-table-column label="项目角色" min-width="100">
          <template #default="{row}">{{ row.roleInProject==='manager'?'负责人':'工程师' }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{row}">
            <el-tag :type="row.status==='confirmed'?'success':'warning'" size="small">
              {{ row.status==='confirmed'?'已确认':'待确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加入时间" min-width="160">
          <template #default="{row}">{{ row.createTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" fixed="right" align="center">
          <template #default="{row}">
            <template v-if="project.status !== 'completed' && (isProjectManager || auth.user?.role=='admin')">
              <el-button v-if="row.status==='confirmed'" text type="danger" size="small"
                @click="handleRemoveMemberClick(row)">移除成员</el-button>
              <el-button v-else text type="warning" size="small"
                @click="handleCancelInviteClick(row)">取消邀请</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showMemberManage=false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 标准模板预览弹窗 -->
    <el-dialog v-model="showTemplateDialog" title="标准阶段模板预览" width="700px" :close-on-click-modal="false" append-to-body align-center :lock-scroll="true">
      <el-alert title="请确认以下阶段信息，可修改后一次性创建" type="info" :closable="false" show-icon style="margin-bottom:16px" />
      <el-alert v-if="hasTemplateDuplicates" :title="`检测到以下阶段名与现有阶段重复：${duplicateStageNames.join('、')}`" type="warning" :closable="false" show-icon style="margin-bottom:16px">
        <template #default>
          <el-checkbox v-model="skipDuplicates" style="margin-top:4px">跳过重复阶段，仅创建新阶段</el-checkbox>
        </template>
      </el-alert>
      <el-form label-width="120px">
        <div v-for="(t, i) in templateStages" :key="i"
             :style="{border:'1px solid ' + (duplicateStageNames.includes(t.stageName) ? '#E6A23C' : 'var(--pm-border)'), borderRadius:'8px', padding:'16px', marginBottom:'12px'}">
          <div style="font-weight:600;margin-bottom:8px;display:flex;align-items:center;justify-content:space-between">
            <span>阶段 {{ i + 1 }}</span>
            <el-tag v-if="duplicateStageNames.includes(t.stageName)" type="warning" size="small">与现有阶段重名</el-tag>
          </div>
          <el-row :gutter="12">
            <el-col :span="16">
              <el-form-item label="阶段名称"><el-input v-model="t.stageName" /></el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="排序"><el-input-number v-model="t.sortOrder" :min="1" :max="99" controls-position="right" style="width:100%" /></el-form-item>
            </el-col>
          </el-row>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { confirmDanger, showActionError } from '../utils/actionGuards'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const projectId = route.params.id

// ---- Tab state ----
const activeTab = ref('overview')

// ---- Data state ----
const loading = ref(true)
const project = ref({})
const stages = ref([])
const members = ref([])
const showAddStage = ref(false)
const showAddMember = ref(false)
const showMemberManage = ref(false)

const closeoutCompleted = ref(0)
const closeoutItems = ref([])

const addingStage = ref(false)
const addingMember = ref(false)
const stageForm = reactive({
  stageName: '', description: '', assigneeId: null,
  planStart: '', planEnd: '', sortOrder: 0
})
const selectedTemplate = ref('')
function onTemplateSelect(name) {
  if (!name) { selectedTemplate.value = ''; return }
  const tpl = templateStages.find(t => t.stageName === name)
  if (tpl) {
    stageForm.stageName = tpl.stageName
    stageForm.description = tpl.description || ''
    stageForm.sortOrder = tpl.sortOrder || 0
  } else {
    stageForm.stageName = name
    stageForm.description = ''
  }
}
const newMember = reactive({ userId: '', roleInProject: 'engineer' })
const availableUsers = ref([])

async function loadAvailableUsers() {
  try {
    const res = await request.get('/users/available')
    availableUsers.value = res.data || []
  } catch {}
}

const changes = ref([])
const showAddChange = ref(false)
const addingChange = ref(false)
const changeForm = reactive({ content: '', confirmTime: '', impact: '' })

// Process tab expand state
const showAllDeviations = ref(false)
const showAllSupports = ref(false)

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

// ---- Computed properties ----
const hasPlanningData = computed(() => {
  return project.value.customerLevel || project.value.contacts || project.value.canUndertake
    || project.value.mainRisks || project.value.teamSetup || project.value.hrAllocation
    || project.value.projectImportance || project.value.achievementDirection
})

const isProjectManager = computed(() => {
  return members.value.some(m => m.userId === auth.user?.id && m.roleInProject === 'manager')
})

const managerNames = computed(() => {
  return members.value
    .filter(m => m.roleInProject === 'manager')
    .map(m => m.realName)
    .filter(Boolean)
    .join('、')
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

const projectSupports = computed(() => {
  return project.value.supportItems || project.value.supports || []
})

// Overview tab computes
const assignedStages = computed(() => {
  if (!auth.user?.id) return []
  return (stages.value || []).filter(s => s.assigneeId === auth.user.id)
})

const recentReports = computed(() => {
  const reports = []
  ;(stages.value || []).forEach(s => {
    if (s.latestReport && s.latestReport.content) {
      reports.push({
        stageName: s.stageName,
        content: s.latestReport.content,
        progressRate: s.latestReport.progressRate || 0,
        time: s.latestReport.createTime || s.latestReport.reportTime || ''
      })
    }
  })
  reports.sort((a, b) => {
    if (a.time && b.time) return b.time.localeCompare(a.time)
    return 0
  })
  return reports.slice(0, 5)
})

const upcomingDeadlines = computed(() => {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  const thirtyDaysLater = new Date(now)
  thirtyDaysLater.setDate(thirtyDaysLater.getDate() + 30)

  return (stages.value || [])
    .filter(s => {
      if (!s.planEnd || s.status === 'completed') return false
      const planEnd = new Date(s.planEnd)
      return planEnd >= now && planEnd <= thirtyDaysLater
    })
    .map(s => {
      const planEnd = new Date(s.planEnd)
      const daysLeft = Math.ceil((planEnd - now) / (1000 * 60 * 60 * 24))
      return { ...s, daysLeft }
    })
    .sort((a, b) => a.daysLeft - b.daysLeft)
})

// ---- Functions ----
function handleEditProject() {
  activeTab.value = 'planning'
  if (!planningEditMode.value) {
    startPlanningEdit()
  }
}

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
  loading.value = true
  try {
    const res = await getProjectDetail(projectId)
    project.value = res.data
  } finally { loading.value = false }
}

async function loadCloseoutStatus() {
  try {
    const res = await request.get(`/projects/${projectId}/closeout-status`)
    const data = res.data
    closeoutCompleted.value = data.completedCount || 0
    closeoutItems.value = [data.review, data.experience, data.approval]
  } catch {}
}
async function loadStages() {
  const res = await getStages(projectId)
  stages.value = res.data
}
async function loadMembers() {
  const res = await getProjectMembers(projectId)
  members.value = res.data
}

function openAddStage() {
  selectedTemplate.value = ''
  Object.assign(stageForm, { stageName: '', description: '', assigneeId: null, planStart: '', planEnd: '', sortOrder: 0 })
  showAddStage.value = true
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
    await confirmDanger(`确认删除阶段"${row.stageName || row.id}"？删除后相关填报也可能受到影响。`, '删除阶段')
    await deleteStage(projectId, row.id)
    ElMessage.success('删除成功')
    loadStages()
  } catch (error) {
    showActionError(error, '删除阶段失败')
  }
}
const showTemplateDialog = ref(false)
const applyingTemplate = ref(false)
const skipDuplicates = ref(true)
const templateStages = reactive([
  { stageName: '外业调查', description: '实地勘测、数据采集、样地调查', planStart: '', planEnd: '', assigneeId: null, sortOrder: 1 },
  { stageName: '内业整理', description: '数据整理、图纸绘制、统计分析', planStart: '', planEnd: '', assigneeId: null, sortOrder: 2 },
  { stageName: '成果编制', description: '编制报告、制作图件', planStart: '', planEnd: '', assigneeId: null, sortOrder: 3 },
  { stageName: '成果提交与审核', description: '提交成果、审核验收', planStart: '', planEnd: '', assigneeId: null, sortOrder: 4 }
])

function openTemplateDialog() {
  templateStages.forEach((t, i) => {
    t.assigneeId = null
    t.sortOrder = i + 1
  })
  templateStages[0].description = '实地勘测、数据采集、样地调查'
  templateStages[1].description = '数据整理、图纸绘制、统计分析'
  templateStages[2].description = '编制报告、制作图件'
  templateStages[3].description = '提交成果、审核验收'
  const today = new Date()
  const baseOffsets = [0, 16, 32, 48]
  const duration = 15
  templateStages.forEach((t, i) => {
    const start = new Date(today)
    start.setDate(start.getDate() + baseOffsets[i])
    const end = new Date(start)
    end.setDate(end.getDate() + duration)
    t.planStart = start.toISOString().slice(0, 10)
    t.planEnd = end.toISOString().slice(0, 10)
  })
  skipDuplicates.value = true
  showTemplateDialog.value = true
}

const duplicateStageNames = computed(() => {
  const existingNames = new Set((stages.value || []).map(s => s.stageName))
  return templateStages.filter(t => existingNames.has(t.stageName)).map(t => t.stageName)
})

const hasTemplateDuplicates = computed(() => duplicateStageNames.value.length > 0)

async function handleApplyTemplate() {
  const missing = templateStages.some(t => !t.assigneeId)
  if (missing) { ElMessage.warning('请为每个阶段选择责任人'); return }
  applyingTemplate.value = true
  try {
    const existingNames = new Set((stages.value || []).map(s => s.stageName))
    const stagesToCreate = skipDuplicates.value
      ? templateStages.filter(t => !existingNames.has(t.stageName))
      : [...templateStages]
    if (stagesToCreate.length === 0) {
      ElMessage.info('没有需要创建的新阶段（所有阶段名已存在且选择了跳过重复）')
      showTemplateDialog.value = false
      return
    }
    const res = await request.post(`/stages/template/${projectId}`, stagesToCreate)
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
async function handleRemoveMemberClick(member) {
  const confirmedManagers = members.value.filter(m => m.roleInProject === 'manager' && m.status === 'confirmed')
  if (member.roleInProject === 'manager' && confirmedManagers.length <= 1) {
    ElMessage.warning('不能移除项目中唯一的负责人')
    return
  }
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `确认移除成员"${member.realName}"（${member.roleInProject === 'manager' ? '负责人' : '工程师'}）？`,
      '移除成员',
      {
        confirmButtonText: '确认移除',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入移除原因',
        inputType: 'textarea',
        inputErrorMessage: '请输入移除原因',
        inputValidator: (val) => val && val.trim() ? true : '请输入移除原因'
      }
    )
    await removeProjectMember(projectId, member.id)
    ElMessage.success('成员已移除')
    loadMembers()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showActionError(error, '移除成员失败')
    }
  }
}
async function handleCancelInviteClick(member) {
  try {
    await ElMessageBox.confirm(
      `确认取消对"${member.realName}"的邀请？`,
      '取消邀请',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await removeProjectMember(projectId, member.id)
    ElMessage.success('邀请已取消')
    loadMembers()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showActionError(error, '取消邀请失败')
    }
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
    await confirmDanger('确认完成该项目？系统将检查结项条件。', '完成项目')
    const res = await request.put(`/projects/${projectId}/complete`)
    ElMessage.success('项目已完成')
    loadProject()
    loadStages()
    loadMembers()
  } catch (error) {
    showActionError(error, '项目结项失败')
  }
}

async function handleReopenProject() {
  try {
    await confirmDanger('确认重新打开该项目？', '重新打开项目')
    await request.put(`/projects/${projectId}/reopen`)
    ElMessage.success('项目已重新打开')
    loadProject()
  } catch (error) {
    showActionError(error, '重新打开失败')
  }
}

function handleExport() {
  window.open(`/api/projects/${projectId}/export`, '_blank')
}

onMounted(() => { loadProject(); loadStages(); loadMembers(); loadChanges(); loadCloseoutStatus() })
</script>

<style scoped>
/* ===== Workspace Page Container ===== */
.ws-page {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* ===== 1. Top Summary Bar ===== */
.ws-topbar {
  position: sticky;
  top: 0;
  z-index: 90;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: nowrap;
  background: var(--pm-surface);
  border-bottom: 1px solid var(--pm-border);
  padding: 10px 20px;
  min-height: 48px;
}

.ws-topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
  min-width: 0;
  overflow: hidden;
}

.ws-topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.ws-project-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--pm-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 320px;
}

.ws-meta-sep {
  color: var(--pm-border-strong);
  font-size: 14px;
  flex-shrink: 0;
}

.ws-meta {
  font-size: 13px;
  color: var(--pm-text-secondary);
  white-space: nowrap;
  flex-shrink: 0;
}

.ws-meta b {
  color: var(--pm-text-muted);
  font-weight: 400;
  margin-right: 6px;
}

.ws-meta--warn {
  color: var(--pm-amber-text);
}

/* ===== 2. Tabs ===== */
.ws-tabs {
  margin: 0;
  border-radius: 0;
  border: none;
}

.ws-tabs :deep(.el-tabs__header) {
  margin: 0;
  border-radius: 0;
  background: var(--pm-surface);
  border-bottom: 1px solid var(--pm-border);
  padding: 0 20px;
}

.ws-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.ws-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  height: 44px;
  line-height: 44px;
  padding: 0 20px;
}

.ws-tabs :deep(.el-tabs__content) {
  padding: 24px 20px;
}

/* ===== Tab Toolbar ===== */
.ws-tab-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 36px;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--pm-border-light);
}

.ws-tab-toolbar-title {
  font-weight: 600;
  font-size: 17px;
  color: var(--pm-text);
}

/* ===== 3. Overview Layout ===== */
.ws-overview {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.ws-overview-main {
  flex: 7;
  min-width: 0;
}

.ws-overview-side {
  flex: 3;
  min-width: 240px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ---- Description ---- */
.ws-desc {
  color: var(--pm-text-secondary);
  font-size: 14px;
  line-height: 1.7;
  margin: 0 0 20px 0;
}

/* ---- Sections ---- */
.ws-section {
  margin-bottom: 24px;
}

.ws-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.ws-section-title {
  font-weight: 600;
  font-size: 17px;
  color: var(--pm-text);
  margin: 0 0 14px 0;
}

/* ---- Action Items ---- */
.ws-action-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ws-action-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: var(--pm-radius);
  background: var(--pm-surface-hover);
  font-size: 14px;
  color: var(--pm-text-secondary);
  min-height: 44px;
}

.ws-action-item--warn {
  background: var(--pm-pale-amber);
  color: var(--pm-amber-text);
}

.ws-action-item--danger {
  background: var(--pm-pale-red);
  color: var(--pm-red-text);
}

.ws-action-item--ok {
  background: var(--pm-pale-green);
  color: var(--pm-green-text);
}

/* ---- Assigned Stages ---- */
.ws-assigned-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ws-assigned-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius);
  min-height: 44px;
  flex-wrap: wrap;
}

.ws-assigned-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 160px;
}

.ws-assigned-name {
  font-weight: 500;
  font-size: 14px;
  color: var(--pm-text);
}

.ws-assigned-progress {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 120px;
}

/* ---- Stage List (compact) ---- */
.ws-stage-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ws-stage-item {
  padding: 12px 14px;
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius);
  transition: border-color var(--pm-duration-fast) var(--pm-ease);
  min-height: 44px;
}

.ws-stage-item:hover {
  border-color: var(--pm-primary);
}

.ws-stage-item-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.ws-stage-item-name {
  font-weight: 500;
  font-size: 14px;
  color: var(--pm-text);
}

.ws-stage-item-pct {
  margin-left: auto;
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-primary);
}

.ws-stage-item-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 6px;
  font-size: 12px;
  color: var(--pm-text-muted);
}

/* ---- Recent Reports ---- */
.ws-report-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ws-report-item {
  padding: 10px 14px;
  border: 1px solid var(--pm-border-light);
  border-radius: var(--pm-radius);
  font-size: 14px;
}

.ws-report-item-hd {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.ws-report-item-stage {
  font-weight: 500;
  color: var(--pm-text);
}

.ws-report-item-time {
  font-size: 12px;
  color: var(--pm-text-muted);
  margin-left: auto;
}

.ws-report-item-body {
  color: var(--pm-text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ---- Deadlines ---- */
.ws-deadline-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ws-deadline-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px;
  border-radius: var(--pm-radius-sm);
  background: var(--pm-surface-hover);
  font-size: 14px;
  min-height: 44px;
}

.ws-deadline-item--urgent {
  background: var(--pm-pale-red);
}

.ws-deadline-name {
  font-weight: 500;
  color: var(--pm-text);
}

.ws-deadline-date {
  margin-left: auto;
  color: var(--pm-text-secondary);
  font-size: 13px;
}

.ws-deadline-days {
  font-size: 13px;
  color: var(--pm-green-text);
  font-weight: 500;
  white-space: nowrap;
}

.ws-deadline-days--urgent {
  color: var(--pm-red-text);
}

/* ===== Overview Sidebar ===== */
.ws-side-card {
  padding: 14px 16px;
  border: 1px solid var(--pm-border-light);
  border-radius: var(--pm-radius);
  background: var(--pm-surface);
}

.ws-side-card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 500;
  color: var(--pm-text);
  margin-bottom: 10px;
}

.ws-side-card-link {
  font-size: 12px;
  color: var(--pm-primary);
  font-weight: 400;
}

.ws-side-members {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ws-side-member {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  min-height: 32px;
}

.ws-side-member-name {
  color: var(--pm-text);
  font-weight: 500;
}

.ws-side-member-role {
  color: var(--pm-text-muted);
  font-size: 12px;
}

.ws-side-stat {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.ws-side-stat-num {
  font-size: 28px;
  font-weight: 600;
}

.ws-side-stat-label {
  font-size: 13px;
  color: var(--pm-text-muted);
}

.ws-side-stat--warn .ws-side-stat-num {
  color: var(--pm-amber-text);
}

.ws-side-stat--ok .ws-side-stat-num {
  color: var(--pm-green-text);
}

.ws-side-closeout {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ws-side-closeout-frac {
  font-size: 22px;
  font-weight: 600;
  color: var(--pm-primary);
}

/* ===== Member Tags ===== */
.member-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.member-tags :deep(.el-tag) {
  height: 30px;
  padding: 0 10px;
  border: 1px solid var(--pm-border);
  background: #f7f8fa;
  color: var(--pm-text-secondary);
  font-size: 13px;
}

/* ===== Edit Planning ===== */
.edit-planning-section .el-divider {
  margin: 20px 0 16px;
}

.edit-planning-section .el-divider:first-child {
  margin-top: 0;
}

.edit-planning-section .el-form-item {
  margin-bottom: 16px;
}

/* ===== Responsive ===== */
@media (max-width: 1200px) {
  .ws-overview {
    flex-direction: column;
  }

  .ws-overview-side {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .ws-topbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    padding: 10px 14px;
  }

  .ws-topbar-left {
    flex-wrap: wrap;
  }

  .ws-topbar-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .ws-project-name {
    font-size: 16px;
    max-width: 200px;
  }

  .ws-tabs :deep(.el-tabs__item) {
    font-size: 13px;
    padding: 0 10px;
  }

  .ws-tabs :deep(.el-tabs__content) {
    padding: 16px 14px;
  }

  .ws-overview {
    flex-direction: column;
  }

  .ws-overview-side {
    width: 100%;
    min-width: 0;
  }

  .ws-assigned-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
