# 四川省林业和草原规划院 项目全过程管理系统 设计文档

## 1. 背景

四川省林业和草原规划院当前使用 Excel 台账管理项目全过程，存在以下问题：

- 数据分散在多个 Excel 文件中，难以统一查看
- 一线人员无法实时反馈阶段进展
- 项目负责人无法及时审阅和纠偏
- 领导层无法快速掌握全院项目异常情况

现有需求文档（`项目全过程管理系统功能需求优先版.md`）已梳理完整业务需求，但领导要求**从简到难**，先跑通核心流程，再逐步增加复杂功能。

## 2. 技术栈

| 层级 | 选型 | 说明 |
|------|------|------|
| 前端框架 | Vue 3 + Vite | 主流组合，热更新快 |
| UI 组件库 | Element Plus | 国内生态好，商务风格 |
| 状态管理 | Pinia | Vue 3 官方推荐 |
| HTTP 请求 | Axios | 成熟方案 |
| 后端框架 | Spring Boot 3.x | 主流 Java 框架 |
| ORM | MyBatis-Plus | 自动建表、代码生成、简化 CRUD |
| 数据库 | MySQL 8.x | 关系型数据库 |
| 认证 | Spring Security + Session | 简单登录，按角色路由 |

## 3. 项目结构

```
项目管理系统/
├── pm-frontend/              # Vue3 前端
│   ├── src/
│   │   ├── views/            # 页面
│   │   ├── components/       # 公共组件
│   │   ├── api/              # 接口封装
│   │   ├── router/           # 路由配置
│   │   └── stores/           # Pinia 状态
│   └── package.json
├── pm-backend/               # SpringBoot 后端
│   ├── src/main/java/com/pm/
│   │   ├── controller/
│   │   ├── service/
│   │   │   └── impl/
│   │   ├── mapper/
│   │   ├── entity/
│   │   └── config/
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── sql/                      # SQL 脚本（手动执行）
│   ├── init.sql              # 初始化建表 + 种子数据
│   └── v0.2.sql              # 各版本的增量SQL
└── docs/
    └── superpowers/
        └── specs/            # 设计文档
```

## 4. 角色设计

| 角色 | 编码 | 职责 |
|------|------|------|
| 系统管理员 | admin | 用户管理、角色管理 |
| 项目负责人 | manager | 创建项目、分配成员、审阅填报、审核成果 |
| 工程师 | engineer | 阶段填报、提交成果 |
| 院领导 | leader | 查看异常项目、只读查看 |

## 5. 版本路线图

### 总览

```
V0.1 (1-2周) → V0.2 (2-3周) → V0.3 (2-3周) → V0.4 (1-2周) → V0.5 (2周)
```

每个版本独立可运行、可演示。

### V0.1 — 核心填报审阅链路（1-2周）

**业务流程：** 登录 → 创建项目 → 添加阶段 → 工程师填报 → 负责人审阅

**数据库表（5张）：**

| 表名 | 用途 |
|------|------|
| sys_user | 用户表（username, password, real_name, role, dept） |
| sys_project | 项目表（name, description, status, create_user_id） |
| sys_project_member | 项目成员（project_id, user_id, role_in_project） |
| sys_project_stage | 项目阶段（project_id, stage_name, plan_start, plan_end, actual_start, actual_end, status, assignee_id） |
| sys_stage_report | 阶段填报（stage_id, project_id, content, progress_rate, problem, actual_start, actual_end, review_status, review_comment, submit_user_id） |

**前端页面（7个）：**

| 页面 | 路由 | 角色 |
|------|------|------|
| 登录页 | /login | 所有人 |
| 项目列表 | /projects | 所有人（按角色过滤） |
| 项目详情 | /projects/:id | 所有人（阶段列表+填报历史） |
| 我的待填报 | /my-tasks | 工程师 |
| 阶段填报 | /my-tasks/:stageId/report | 工程师 |
| 待审阅列表 | /pending-review | 负责人 |
| 阶段审阅 | /pending-review/:reportId | 负责人 |

**核心接口：**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录 |
| POST | /api/auth/logout | 退出 |
| GET | /api/auth/current-user | 获取当前用户 |
| GET | /api/projects | 项目列表（分页、搜索） |
| POST | /api/projects | 创建项目 |
| GET | /api/projects/{id} | 项目详情（含阶段+成员） |
| PUT | /api/projects/{id} | 更新项目 |
| DELETE | /api/projects/{id} | 删除项目 |
| POST | /api/projects/{id}/members | 添加项目成员 |
| DELETE | /api/projects/{id}/members/{memberId} | 移除成员 |
| GET | /api/projects/{id}/stages | 项目阶段列表 |
| POST | /api/projects/{id}/stages | 添加阶段 |
| PUT | /api/projects/{id}/stages/{stageId} | 更新阶段 |
| DELETE | /api/projects/{id}/stages/{stageId} | 删除阶段 |
| GET | /api/stages/my-tasks | 我的待填报 |
| POST | /api/stages/{stageId}/reports | 提交阶段填报 |
| GET | /api/stages/{stageId}/reports | 阶段填报历史 |
| GET | /api/reports/pending | 负责人待审阅 |
| POST | /api/reports/{reportId}/review | 审阅（通过/退回） |

**可向领导演示的内容：** 登录后不同角色看到不同首页；创建项目并自由添加阶段；工程师提交进展；负责人审阅通过/退回；退回后重新填报的闭环。

---

### V0.2 — 成果提交与审核闭环（1-2周）

**思路：** 不新建表，扩展现有 阶段填报 链路，加入文件附件能力。成果提交在Excel台账中就是执行阶段的一个子项，和"外业调查""内业整理"并列。

**数据库变更：**
- `sys_stage_report` 新增字段：`attachment_name VARCHAR(500)` 附件文件名，`attachment_data MEDIUMBLOB` 附件二进制数据

**后端改动：**
- `SysStageReport` 实体新增 `attachmentName`、`attachmentData` 字段（attachmentData 不返回给列表接口）
- `ReportController` 文件上传改为在提交 `POST /api/stages/{stageId}/reports` 时一并上传（multipart）
- 新增 `GET /api/reports/{reportId}/attachment` 文件下载接口

**前端改动（2个页面改动 + 1个新页面）：**

| 页面 | 改动 |
|------|------|
| StageReport.vue | 填报表单增加 el-upload 文件上传（必选） |
| PendingReview.vue | 列表增加"附件"列，区分有附件的成果提交 |
| AchievementReview.vue（新） | 审核页：展示填报内容+附件下载+通过/退回 |

**可汇报：** 工程师可以在阶段填报时上传成果文件（Word/PDF/图纸），负责人审核时可下载查看附件，审核通过/退回形成正式闭环。

---

### V0.3 — 偏差管理与工作台（2-3周）

**核心设计：** 偏差自动生成+手动创建结合，支持事项独立申请入口，登录首页改为角色工作台。

**新增表（2张）：**

- `sys_deviation`：id, project_id, stage_id, report_id, type(auto/manual), description, reason, impact, status(open/closed), create_user_id, create_time, close_time
- `sys_support_item`：id, project_id, title, content, applicant_id, handler_id, expect_time, status(pending/resolved), reply, create_time, update_time

**页面改动：**

| 操作 | 文件 | 说明 |
|------|------|------|
| 新建 | Dashboard.vue | 角色工作台首页（统计卡片+快捷入口） |
| 新建 | DeviationList.vue | 偏差台账列表（筛选+关闭） |
| 新建 | SupportList.vue | 支持事项列表 |
| 新建 | SupportForm.vue | 新建/处理支持申请 |
| 改 | StageReport.vue | 加勾选框"标记为偏差" |
| 改 | MainLayout.vue | 侧边栏加"偏差管理""支持事项"菜单 |
| 改 | router/index.js | 加路由+首页改为 /dashboard |

**可汇报：** 每个角色登录即看到专属工作台（统计数据+待办入口），阶段填报延期自动生成偏差记录形成偏差台账，需要上级协调的事可发起支持申请并追踪处理。

**可汇报：** 角色专属工作台、延期自动生成偏差记录、支持申请正式流程。

---

### V0.4 — 领导看板与收尾复盘（1-2周）

**新增页面（4个）：**

| 页面 | 说明 |
|------|------|
| 领导看板 | 高风险项目、逾期项目、未填报项目、待支持事项（只读） |
| 项目自评 | 负责人填写执行偏差、效率、质量评价、沟通情况 |
| 经验总结 | 项目结项后填写经验/短板/风险/改进建议 |
| 经验库 | 已结项项目经验汇总 |

**新增表：** `sys_review`（自评）、`sys_experience`（经验总结）

**可汇报：** 领导看板掌握全院项目异常、项目结束后自动沉淀经验形成知识库。

---

### V0.5 — Excel 导出与打磨（~2周）

| 功能 | 说明 |
|------|------|
| 项目台账导出 | 导出单个项目全过程为Excel |
| 项目列表导出 | 批量导出项目列表 |
| UI 打磨 | 统一商务风格、优化交互 |
| Bug 修复 | 前版本问题修复 |

---

## 6. 非功能需求

- 新建数据库，MyBatis-Plus 自动建表，SQL 脚本放 `sql/` 目录手动执行
- 登录用 Spring Security + Session，不做 JWT
- 前端路由按角色控制，登录后根据角色跳不同首页
- UI要求：Element Plus 商务风格，简洁专业
