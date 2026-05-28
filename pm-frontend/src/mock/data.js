// Mock data store for demo mode (no backend needed)

let currentUser = null

const users = {
  'manager1': { id: 2, username: 'manager1', realName: '张主任', role: 'manager', dept: '规划一室' },
  'engineer1': { id: 3, username: 'engineer1', realName: '李工', role: 'engineer', dept: '规划一室' },
  'leader1': { id: 4, username: 'leader1', realName: '王院长', role: 'leader', dept: '院领导' },
  'admin': { id: 1, username: 'admin', realName: '系统管理员', role: 'admin', dept: '信息中心' }
}

const projects = [
  { id: 1, name: '古蔺县森林资源调查与空间规划', description: '对古蔺县全域森林资源进行调查评估，编制林地保护利用规划，涉及15人团队、外业调查30天、内业整理25天、成果编制20天。', status: 'active', createUserId: 2, createTime: '2026-05-10 09:30:00' },
  { id: 2, name: '攀枝花市林业产业发展规划', description: '攀枝花市林业产业"十五五"发展规划编制，重点围绕特色经济林、林下经济、森林康养三大方向。', status: 'active', createUserId: 2, createTime: '2026-05-15 14:00:00' },
  { id: 3, name: '阿坝州草原生态修复项目实施方案', description: '阿坝州退化草原生态修复实施方案编制，涉及鼠荒地治理、人工种草、围栏封育等工程措施。', status: 'active', createUserId: 2, createTime: '2026-05-20 10:00:00' }
]

const projectMembers = {
  1: [
    { id: 1, userId: 2, realName: '张主任', dept: '规划一室', roleInProject: 'manager' },
    { id: 2, userId: 3, realName: '李工', dept: '规划一室', roleInProject: 'engineer' }
  ],
  2: [
    { id: 3, userId: 2, realName: '张主任', dept: '规划一室', roleInProject: 'manager' },
    { id: 4, userId: 3, realName: '李工', dept: '规划一室', roleInProject: 'engineer' }
  ],
  3: [
    { id: 5, userId: 2, realName: '张主任', dept: '规划一室', roleInProject: 'manager' },
    { id: 6, userId: 3, realName: '李工', dept: '规划一室', roleInProject: 'engineer' }
  ]
}

const stages = {
  1: [
    { id: 1, projectId: 1, stageName: '外业调查', description: '古蔺县全域林地资源外业调查，涉及面积测量、树种识别、蓄积量估算', planStart: '2026-05-15', planEnd: '2026-06-15', actualStart: '2026-05-16', actualEnd: null, status: 'submitted', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: { id: 1, content: '已完成古蔺县南部片区（观文镇、椒园镇）外业调查，面积约15万亩，设置样地320个，树种以马尾松、杉木、柏木为主，蓄积量约85万立方米。', progressRate: 65, problem: '北部片区地形复杂，部分区域交通不便，预计将延迟5天。需要协调当地林业站配合。', reviewStatus: 'pending' } },
    { id: 2, projectId: 1, stageName: '内业整理', description: '外业数据整理、图纸绘制、统计分析', planStart: '2026-06-16', planEnd: '2026-07-10', actualStart: null, actualEnd: null, status: 'pending', assigneeId: 3, assigneeName: '李工', sortOrder: 2, latestReport: null },
    { id: 3, projectId: 1, stageName: '成果提交', description: '编制调查报告、图件、统计表', planStart: '2026-07-11', planEnd: '2026-07-31', actualStart: null, actualEnd: null, status: 'pending', assigneeId: 3, assigneeName: '李工', sortOrder: 3, latestReport: null }
  ],
  2: [
    { id: 4, projectId: 2, stageName: '前期调研', description: '攀枝花市域林业产业现状调研，重点走访仁和区、米易县', planStart: '2026-05-20', planEnd: '2026-06-10', actualStart: '2026-05-21', actualEnd: null, status: 'in_progress', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: null },
    { id: 5, projectId: 2, stageName: '规划编制', description: '编写发展规划文本及说明', planStart: '2026-06-11', planEnd: '2026-07-15', actualStart: null, actualEnd: null, status: 'pending', assigneeId: 3, assigneeName: '李工', sortOrder: 2, latestReport: null }
  ],
  3: [
    { id: 6, projectId: 3, stageName: '外业调查', description: '阿坝州草原退化现状调查', planStart: '2026-06-01', planEnd: '2026-07-01', actualStart: null, actualEnd: null, status: 'pending', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: null }
  ]
}

let reportIdCounter = 100
const reports = {
  1: [
    { id: 1, stageId: 1, projectId: 1, content: '已完成古蔺县南部片区（观文镇、椒园镇）外业调查，面积约15万亩，设置样地320个，树种以马尾松、杉木、柏木为主，蓄积量约85万立方米。', progressRate: 65, problem: '北部片区地形复杂，部分区域交通不便，预计将延迟5天。需要协调当地林业站配合。', actualStart: '2026-05-16', actualEnd: null, reviewStatus: 'pending', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-05-28 16:30:00', reviewUserId: null, reviewTime: null, createTime: '2026-05-28 16:30:00', attachmentName: '古蔺南部调查报告_初稿.pdf', stageName: '外业调查', projectName: '古蔺县森林资源调查与空间规划' }
  ]
}

const deviations = [
  { id: 1, projectId: 1, stageId: 1, reportId: 1, type: 'auto', description: '外业调查北部片区因交通不便延迟5天，当前仅完成65%', reason: '地形复杂，部分区域交通不便，雨天无法作业', impact: '整体工期延后约5天，后续内业整理需压缩时间', status: 'open', createUserId: 2, createUserName: '张主任', createTime: '2026-05-28 17:00:00', closeTime: null, projectName: '古蔺县森林资源调查与空间规划', stageName: '外业调查' },
  { id: 2, projectId: 2, stageId: 4, reportId: null, type: 'manual', description: '攀枝花市前期调研因对接人不明确导致启动延迟3天', reason: '项目对接人变更，信息交接不充分', impact: '后续规划编制时间需顺延3天', status: 'open', createUserId: 2, createUserName: '张主任', createTime: '2026-05-25 10:00:00', closeTime: null, projectName: '攀枝花市林业产业发展规划', stageName: '前期调研' }
]

const supportItems = [
  { id: 1, projectId: 1, title: '需要院领导协调当地林业局配合外业调查', content: '古蔺县北部片区外业调查需要当地林业局提供林地权属资料和配合现场踏勘。目前与地方林业站沟通不畅，对方以资料涉密为由拖延提供，需要院级层面发函协调。', applicantId: 3, applicantName: '李工', handlerId: 2, handlerName: '张主任', expectTime: '2026-06-05', status: 'pending', reply: null, createTime: '2026-05-28 17:30:00', updateTime: '2026-05-28 17:30:00', projectName: '古蔺县森林资源调查与空间规划' },
  { id: 2, projectId: 3, title: '草原生态修复方案需要外部专家评审', content: '阿坝州草原生态修复项目涉及高寒草甸恢复技术，院内缺乏该领域专家，需要邀请川农大草原研究所专家参与方案评审。', applicantId: 3, applicantName: '李工', handlerId: 2, handlerName: '张主任', expectTime: '2026-06-15', status: 'pending', reply: null, createTime: '2026-05-26 14:00:00', updateTime: '2026-05-26 14:00:00', projectName: '阿坝州草原生态修复项目实施方案' }
]

const reviews = {
  1: { id: 1, projectId: 1, overallDeviation: '外业偏差15天，内业偏差30天，成果偏差30天。前期对工作量预估不足，导致多次延期。', efficiencyRating: '整体效率偏低。前期对地形复杂度和协调难度预估不足，外业阶段雨天停工较多，内业阶段因数据不完整多次返工。建议后续类似项目增加15%的时间buffer。', qualityRating: '成果质量整体满足要求，但图件标注存在少量错误，报告文字有少量纰漏。经专家审核后已修正，最终通过评审。外业数据采集较为扎实，为后续分析提供了良好基础。', communicationNote: '与业主的沟通总体顺畅，关键节点均及时汇报。但在外业调查阶段与地方林业站的协调不够，导致资料获取延迟。院级层面介入后问题得到解决。后续建议在项目启动期就建立多方沟通机制。', createUserId: 2, createTime: '2026-07-25 10:00:00', projectName: '古蔺县森林资源调查与空间规划' }
}

const experiences = {
  1: { id: 1, projectId: 1, reusableExperience: '1. 古蔺项目的外业调查分组模式值得复用——按地形单元分4个小组并行推进，每组配置林业+测绘+地理专业人员，效率提升明显。\n2. 建立了完整的林地资源数据库模板，包括样地调查表、树种分布图、蓄积量统计表等，可直接复用于类似项目。\n3. 与业主的定期沟通机制（每周五下午进度通报）有效减少了后期返工。', shortcomings: '1. 前期对地形复杂度评估不足，北部山区交通条件差，导致外业延迟。\n2. 内业阶段数据分析人才配置不足，过度依赖1-2名骨干，形成瓶颈。\n3. 缺乏标准化的质量审核流程，第一版成果内部审核不够严格。', risks: '1. 地形复杂区域的外业调查时间窗口受天气影响大，需预留弹性时间。\n2. 地方数据获取存在行政壁垒，需要提前建立协调机制。\n3. 成果编制阶段多专业协作容易出现衔接遗漏。', improvement: '1. 建议在项目启动前进行实地踏勘，准确评估调查难度和时间。\n2. 建立内部三级审核制度（自审→互审→专家审），提高成果质量。\n3. 推广古蔺项目的分组并行作业模式到其他大型调查项目。\n4. 加强与地方林业部门的常态化联系，降低数据获取成本。', createUserId: 2, createTime: '2026-07-25 11:00:00', projectName: '古蔺县森林资源调查与空间规划' }
}

export function getMockData() {
  return { users, projects, projectMembers, stages, reports, deviations, supportItems, reviews, experiences, currentUser }
}

export function setMockUser(username) {
  currentUser = users[username] || null
  return currentUser
}

export function getMockUser() {
  return currentUser
}

export function nextReportId() {
  return reportIdCounter++
}
