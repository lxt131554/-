# Demo Professionalization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将当前 mock 演示版升级为可向院领导展示的专业项目管理平台界面，重点提升统一风格、核心看板、项目详情、流程页和演示数据叙事。

**Architecture:** 保持现有 Vue 3 + Element Plus + mock API 结构不变，不新增后端依赖，不改接口协议。通过统一全局样式、重构核心页面布局、补齐演示型摘要区和更可信的 mock 数据，形成一条完整演示链路。

**Tech Stack:** Vue 3, Vite, Element Plus, Pinia, mock data (`src/mock/data.js`)

---

## Scope Guard

执行前先确认以下边界，任何一步都不要越界：

- 保留 `pm-frontend/.env.production` 中的 mock 演示策略；
- 不改后端接口路径与参数格式；
- 不做数据库、部署、附件存储方案改造；
- 不大规模调整路由结构；
- 不把页面做成营销风或大卡片拼贴风。

## File Map

### Must Modify

- `I:\规划院\项目管理系统\pm-frontend\src\style.css`
- `I:\规划院\项目管理系统\pm-frontend\src\layouts\MainLayout.vue`
- `I:\规划院\项目管理系统\pm-frontend\src\views\Dashboard.vue`
- `I:\规划院\项目管理系统\pm-frontend\src\views\LeaderDashboard.vue`
- `I:\规划院\项目管理系统\pm-frontend\src\views\ProjectDetail.vue`
- `I:\规划院\项目管理系统\pm-frontend\src\views\StageReport.vue`
- `I:\规划院\项目管理系统\pm-frontend\src\views\PendingReview.vue`
- `I:\规划院\项目管理系统\pm-frontend\src\views\ProjectList.vue`
- `I:\规划院\项目管理系统\pm-frontend\src\views\DeviationList.vue`
- `I:\规划院\项目管理系统\pm-frontend\src\views\SupportList.vue`
- `I:\规划院\项目管理系统\pm-frontend\src\mock\data.js`

### Optional Small Helpers

- `I:\规划院\项目管理系统\pm-frontend\src\utils\*.js`
- `I:\规划院\项目管理系统\pm-frontend\src\api\*.js`

只有在复用状态映射、摘要计算、格式化逻辑时才新增 helper，避免新建一堆空抽象。

## Task 1: 统一视觉骨架

**Files:**
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\style.css`
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\layouts\MainLayout.vue`

- [ ] **Step 1: 先清理全局样式目标**

确认以下全局块都存在统一定义：

```css
.page-container {}
.page-header {}
.page-summary-grid {}
.summary-card {}
.filter-bar {}
.section-block {}
.section-title {}
.table-toolbar {}
```

目标不是继续堆更多“高级感”变量，而是建立能在所有页面复用的结构类。

- [ ] **Step 2: 调整全局字体、间距、表格和卡片节奏**

在 `style.css` 中完成以下方向：

```css
body { font-size: 14px; line-height: 1.6; }
.page-header h2 { font-size: 28px; }
.summary-card { min-height: 120px; }
.filter-bar { display: flex; flex-wrap: wrap; gap: 12px; }
.section-block { background: var(--pm-surface); border: 1px solid var(--pm-border); }
```

要求：

- 字号回到商务系统常用密度；
- 表格表头不要全大写英文式语气；
- 卡片不做双层装饰边框泛滥；
- 所有核心块的圆角控制在 8px 以内。

- [ ] **Step 3: 重构主布局**

在 `MainLayout.vue` 中把品牌、分组、用户信息、通知区调整成更正式的院内系统布局，至少形成以下结构：

```vue
<div class="sidebar-brand">
  <div class="brand-title">林草规划院项目管理系统</div>
  <div class="brand-subtitle">Project Lifecycle Platform</div>
</div>
```

并完成：

- 左侧菜单分组标题更清晰；
- 顶栏右侧显示姓名、角色、待办入口；
- 主内容区保留稳定留白，不出现漂浮感过强的容器。

- [ ] **Step 4: 本任务自检**

手动检查以下页面：

- `/dashboard`
- `/leader-dashboard`
- `/projects`

预期：

- 三个页面标题、留白、卡片、表格风格一致；
- 左右结构稳定；
- 没有过大字号、过厚阴影、明显装饰风。

- [ ] **Step 5: Commit**

```bash
git add pm-frontend/src/style.css pm-frontend/src/layouts/MainLayout.vue
git commit -m "feat: unify demo visual shell"
```

## Task 2: 重做负责人工作台

**Files:**
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\views\Dashboard.vue`
- Optional Modify: `I:\规划院\项目管理系统\pm-frontend\src\mock\data.js`

- [ ] **Step 1: 重新组织页面结构**

把页面调整为四段：

```vue
<section class="page-header" />
<section class="page-summary-grid" />
<section class="section-block">重点项目</section>
<section class="section-block">待办与异常</section>
```

不要再保留当前“纯数字大卡 + 下方零散按钮”的展示方式。

- [ ] **Step 2: 让卡片表达业务判断**

负责人首页至少展示：

- 我负责项目数
- 待审核填报
- 未关闭偏差
- 待处理支持事项
- 本周临近节点

卡片文案要能表达状态，例如：

```js
[
  { label: '我负责项目', hint: '当前跟进中的项目数量' },
  { label: '待审核填报', hint: '需要今天处理的审核事项' },
  { label: '未关闭偏差', hint: '影响进度或成果质量的异常' }
]
```

- [ ] **Step 3: 增加“重点项目列表”而不是只放快捷按钮**

新增一个简表，字段建议：

```js
['项目名称', '当前阶段', '计划节点', '风险状态', '下一动作']
```

让负责人一眼知道该先处理哪个项目。

- [ ] **Step 4: 保留角色分支但统一体验**

继续支持 engineer / manager / leader 分支，但视觉组件必须统一。

要求：

- engineer 首页像“我的任务面板”；
- manager 首页像“项目运营面板”；
- 不能每种角色一套完全不同的视觉语言。

- [ ] **Step 5: 本任务自检**

手动登录至少验证：

- `manager1`
- `engineer1`

预期：

- 两种角色首页都清晰；
- 能解释“现在该做什么”；
- 没有空洞大数字墙。

- [ ] **Step 6: Commit**

```bash
git add pm-frontend/src/views/Dashboard.vue pm-frontend/src/mock/data.js
git commit -m "feat: redesign manager and engineer dashboard"
```

## Task 3: 重做领导看板

**Files:**
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\views\LeaderDashboard.vue`
- Optional Modify: `I:\规划院\项目管理系统\pm-frontend\src\mock\data.js`

- [ ] **Step 1: 调整为“总览 + 风险 + 项目”三层结构**

目标结构：

```vue
<section class="page-summary-grid">全院概览</section>
<section class="section-block">重点风险事项</section>
<section class="section-block">需关注项目</section>
```

- [ ] **Step 2: 将统计项改为领导语言**

优先显示：

- 在建项目
- 高风险项目
- 待协调支持事项
- 未关闭偏差
- 已结项项目

避免使用负责人视角的“待我审核”等表达。

- [ ] **Step 3: 增加“需关注项目”区域**

至少展示 5 条以内重点项目，每条含：

```js
{
  name: '项目名',
  issue: '当前主要问题',
  owner: '负责人',
  action: '建议领导关注点'
}
```

这个区域是领导页最重要的部分，不要只是重复项目总表。

- [ ] **Step 4: 压缩非重点明细**

偏差表、支持事项表、变更表都只保留最重要的列，推荐不超过 4-5 列，避免领导页看起来像操作后台。

- [ ] **Step 5: 本任务自检**

登录 `leader1` 检查：

- 首页首屏能看出全院项目态势；
- 至少有 1-2 个明确异常点；
- 可以自然讲出“领导为什么要看这个页”。

- [ ] **Step 6: Commit**

```bash
git add pm-frontend/src/views/LeaderDashboard.vue pm-frontend/src/mock/data.js
git commit -m "feat: redesign leadership dashboard"
```

## Task 4: 重做项目详情页

**Files:**
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\views\ProjectDetail.vue`

- [ ] **Step 1: 新建项目概览头部**

在标题区下方增加摘要区，至少包含：

```js
['项目状态', '当前阶段', '计划完成时间', '未关闭偏差', '支持事项']
```

如果现有数据不全，可用已有 `stages`、`changes`、`members`、`project.status` 计算近似值。

- [ ] **Step 2: 重排“启动策划信息”**

现有 `el-descriptions` 信息量很大，改为按分组显示：

- 客户与立项判断
- 前期分析与约束
- 策划与资源配置
- 项目获取与审批

每组控制在 4-6 项，不要一屏堆满长描述表。

- [ ] **Step 3: 强化阶段区**

阶段列表要更强调：

- 阶段名称
- 责任人
- 计划/实际时间
- 当前状态
- 最近一次填报摘要

推荐加入简短进度提示，例如：

```js
statusTextMap = {
  pending: '待开始',
  in_progress: '执行中',
  submitted: '待审核',
  completed: '已完成'
}
```

- [ ] **Step 4: 把变更、成员、复盘做成正式分区**

要求：

- 变更控制区有明确标题和状态说明；
- 成员区能看出角色分工；
- 已完成项目的复盘入口更像“结项工作”而不是底部按钮。

- [ ] **Step 5: 本任务自检**

打开至少两个项目：

- 一个在建项目；
- 一个已完成项目；

预期：

- 项目详情能自然展示“从启动到收尾”的全过程；
- 已完成项目能看到结项闭环；
- 在建项目能看出当前处于哪一步。

- [ ] **Step 6: Commit**

```bash
git add pm-frontend/src/views/ProjectDetail.vue
git commit -m "feat: rebuild project detail narrative"
```

## Task 5: 重做阶段填报与待审核页

**Files:**
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\views\StageReport.vue`
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\views\PendingReview.vue`

- [ ] **Step 1: 重组 StageReport 表单**

表单分成 5 组：

```js
[
  '进度与时间',
  '工作内容',
  '风险与偏差',
  '质量控制与成果',
  '附件与提交'
]
```

不要把所有字段平铺成一长列。

- [ ] **Step 2: 提升业务字段可读性**

重点字段要有更明确的文案：

- 当前完成情况
- 存在问题或偏差原因
- 质量控制措施
- 阶段成果说明
- 附件材料

如果页面里存在“看上去像技术字段名”的标签，统一改成业务语言。

- [ ] **Step 3: 改造历史填报区**

历史记录区不要只是时间线文本，至少增加：

```js
['提交时间', '进度', '审核状态', '主要内容', '审核意见']
```

可以继续使用时间线，但每条记录应更像一份提交记录卡。

- [ ] **Step 4: 重做 PendingReview**

待审核页要至少具备：

- 顶部摘要卡：待审核数量、超时数量、成果附件数量；
- 筛选：项目、状态、是否有偏差；
- 主表格：项目、阶段、提交人、时间、偏差、动作。

- [ ] **Step 5: 本任务自检**

检查：

- 工程师进入填报页时知道要填什么；
- 负责人进入待审核页时知道先审哪个；
- 页面不再像简易表单。

- [ ] **Step 6: Commit**

```bash
git add pm-frontend/src/views/StageReport.vue pm-frontend/src/views/PendingReview.vue
git commit -m "feat: redesign reporting and review flow"
```

## Task 6: 统一列表页并补强 demo 数据

**Files:**
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\views\ProjectList.vue`
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\views\DeviationList.vue`
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\views\SupportList.vue`
- Modify: `I:\规划院\项目管理系统\pm-frontend\src\mock\data.js`

- [ ] **Step 1: 给三个列表页增加统一框架**

每个列表页至少包含：

```vue
<div class="page-header" />
<div class="page-summary-grid" />
<div class="filter-bar" />
<div class="section-block">
  <el-table />
</div>
```

- [ ] **Step 2: 统一状态标签和摘要口径**

三页都用统一状态色：

```js
{
  normal: 'success',
  warning: 'warning',
  risk: 'danger',
  done: 'info'
}
```

不要每页自己定义一套颜色含义。

- [ ] **Step 3: 改写 mock 项目故事线**

在 `mock/data.js` 中确保至少有以下项目叙事：

```js
[
  '正常推进的规划类项目',
  '存在延期偏差的调查类项目',
  '需要院级协调支持的生态修复项目',
  '已结项并有复盘经验的院内数字化项目'
]
```

并保证：

- `projects`
- `stages`
- `reports`
- `deviations`
- `supportItems`
- `reviews`
- `experiences`

之间能互相对上。

- [ ] **Step 4: 删除“看起来像随机生成”的描述**

逐条检查项目描述、偏差原因、支持事项标题，把过于泛的内容改成业务化表述，例如：

```js
'需协调当地林业局提供样地权属资料'
'高寒地区连续降雨导致外业窗口缩短'
'成果图件比例尺与统计表口径需再次核对'
```

- [ ] **Step 5: 本任务自检**

从首页点击进入：

- 一个正常项目；
- 一个有偏差项目；
- 一个有支持事项项目；
- 一个已完成项目；

预期：

- 所有页面之间的数据口径连贯；
- 不会出现首页说有问题、详情页却毫无痕迹的情况。

- [ ] **Step 6: Commit**

```bash
git add pm-frontend/src/views/ProjectList.vue pm-frontend/src/views/DeviationList.vue pm-frontend/src/views/SupportList.vue pm-frontend/src/mock/data.js
git commit -m "feat: align list pages and demo story data"
```

## Task 7: 演示验收与回归检查

**Files:**
- No code changes required unless defects found

- [ ] **Step 1: 按角色走一遍演示链路**

至少检查三个账号：

```text
manager1
engineer1
leader1
```

每个角色完成一条关键路径。

- [ ] **Step 2: 走负责人演示链路**

路径：

```text
/dashboard -> /projects -> /projects/:id -> /my-tasks/:stageId/report -> /pending-review
```

预期：

- 能讲清项目状态、阶段进度、风险偏差、待审动作。

- [ ] **Step 3: 走领导演示链路**

路径：

```text
/leader-dashboard -> /deviations -> /supports -> /projects/:id
```

预期：

- 能讲清全院态势、重点异常、需要领导关注的项目。

- [ ] **Step 4: 做视觉回归检查**

手动确认：

- 文字未溢出；
- 表格列宽基本稳定；
- 卡片高度不会因文案长短大幅跳动；
- 首屏没有大面积空白或明显拥挤。

- [ ] **Step 5: 记录遗留问题**

把本轮未处理但明显存在的问题列成短清单，限定为 5 条以内，写在交付说明中，供下一轮继续做。重点记录：

- 真后端接入前的限制；
- Excel 模板化未完成；
- 权限与附件正式方案未展开。

- [ ] **Step 6: Commit**

```bash
git add .
git commit -m "chore: polish demo flow acceptance pass"
```

## Acceptance Checklist

- [ ] 首页、领导页、项目详情页属于同一套产品语言
- [ ] 页面明显体现“全过程管理”而不是普通增删改查
- [ ] mock 数据足够真实，支持一条 3 分钟演示主线
- [ ] manager / engineer / leader 三种角色体验都成立
- [ ] 未破坏当前 mock 演示模式
- [ ] 未引入部署、后端、数据库层面的额外变更

## Handoff Notes

执行者不要自行扩大范围。这个计划的核心价值是让现有演示版“像真系统”，不是把所有中长期建设一起做掉。优先做出首屏观感、项目叙事和页面一致性，再考虑更深层的业务改造。
