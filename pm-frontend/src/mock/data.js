// Mock data store for demo mode (no backend needed)

let currentUser = null

const users = {
  'manager1': { id: 2, username: 'manager1', realName: '张主任', role: 'manager', dept: '规划一室', enabled: true },
  'engineer1': { id: 3, username: 'engineer1', realName: '李工', role: 'engineer', dept: '规划一室', enabled: true },
  'leader1': { id: 4, username: 'leader1', realName: '王院长', role: 'leader', dept: '院领导', enabled: true },
  'admin': { id: 1, username: 'admin', realName: '系统管理员', role: 'admin', dept: '信息中心', enabled: true }
}

const projects = [
  { id: 1, name: '古蔺县森林资源调查与空间规划', description: '对古蔺县全域26个乡镇森林资源进行调查评估，编制林地保护利用规划（2026-2035年），涉及外业调查30天、内业整理25天、成果编制20天，15人团队。', status: 'active', createUserId: 2, createTime: '2026-05-10 09:30:00',
    customerLevel: 'A（重大项目）', contacts: '甲方：古蔺县自然资源局张局长\n乙方：院规划一室张主任', achievementType: '林地保护利用规划文本、调查报告、图件集、统计表', approvalRequirements: '需通过县自然资源局初审、省林业局审批、县政府常务会审查', canUndertake: '项目涉及专业齐全（林业、测绘、遥感、GIS），院内有足够技术力量，可以承接', mainRisks: '1.北部山区地形复杂导致外业延期\n2.地方林业站资料配合度不确定\n3.夏秋季降雨影响作业效率', keyConstraints: '1.工期8个月\n2.需协调多部门\n3.成果需通过省林业局审批', deliverableRequirements: '纸质报告10套、电子版光盘5份、图件PDF和CAD格式', approvalPath: '院内部三审→专家评审→县自然资源局审批→省林业局备案', hrAllocation: '项目负责人1人、外业工程师5人、内业工程师3人、制图2人、GIS分析师2人', expectedOutputs: '1.项目建议书\n2.调查报告\n3.规划文本\n4.图件集\n5.汇报PPT', coreMaterials: '1.国土三调数据\n2.林地一张图\n3.森林资源二类调查数据\n4.统计年鉴', teamSetup: '以张主任为组长，15人团队，分4个专业组', coreStrategy: '1.资料比对与实地调查结合\n2.分组并行作业\n3.关键节点向院领导专题汇报', bidSituation: '2026年4月竞争性谈判中标，参与单位3家', procurementInfo: '古蔺县自然资源局公开招标', acquisitionResult: '成功中标，合同金额120万元', projectImportance: '甲方年度森林资源考核重点项目；我院拓展县域林草规划业务的关键机会', achievementDirection: '规划文本提交省林业局备案、图件提交县自然资源局入库' },
  { id: 2, name: '规划院项目全过程管理系统', description: '规划院内部数字化转型重点项目，基于SpringBoot+Vue3开发项目全过程管理系统，覆盖启动策划到收尾复盘的全部业务场景，替代原有Excel手工台账。', status: 'completed', createUserId: 2, createTime: '2026-03-01 09:00:00',
    customerLevel: 'S（战略性项目）', contacts: '项目发起：院信息中心王院长\n项目负责：规划一室张主任', achievementType: 'Web应用系统、数据库系统、用户操作手册', approvalRequirements: '需通过院信息化领导小组审查、院务会审议通过', canUndertake: '院内自有项目，信息中心具备开发能力，可以承担', mainRisks: '1.业务需求复杂多变\n2.新老系统切换风险\n3.用户培训推广', keyConstraints: '1.预算有限（院内自筹）\n2.需在3个月内完成核心功能', deliverableRequirements: '系统源码、部署文档、用户操作手册、培训视频', approvalPath: '信息中心自审→院信息化领导小组评审→试运行→院务会审议', hrAllocation: '项目经理1人、前端2人、后端2人、UI设计1人、测试1人', expectedOutputs: '1.需求规格说明书\n2.系统设计文档\n3.V0.1-V1.0迭代开发', coreMaterials: '1.原Excel台账模板\n2.院业务流程规范文件', teamSetup: '信息中心为主，联合规划一室，共8人，敏捷开发', coreStrategy: '1.先跑通核心流程再丰富功能\n2.主流开源技术栈\n3.分批上线快速迭代', bidSituation: '院内项目，无需招标', procurementInfo: '院内自行开发', acquisitionResult: '2026年3月院务会批准立项', projectImportance: '我院数字化转型的标杆项目', achievementDirection: '部署在院内服务器，后续扩展移动端和外部访问' },
  { id: 3, name: '攀枝花市林业产业发展规划', description: '攀枝花市林业产业"十五五"发展规划编制，重点围绕特色经济林（芒果、核桃）、林下经济（食用菌、中药材）、森林康养三大方向。', status: 'active', createUserId: 2, createTime: '2026-05-20 10:00:00',
    customerLevel: 'A（重大项目）', contacts: '甲方：攀枝花市林业局产业科刘科长\n乙方：院规划一室张主任', achievementType: '林业产业发展规划文本、产业布局图、重点项目清单', approvalRequirements: '需通过市林业局审查、市发改委审核、市政府常务会批准', canUndertake: '院内有林业经济专业人才，前期已完成多个产业规划项目', mainRisks: '1.产业数据获取难度大\n2.林下经济方向专业人才不足\n3.审批层级多周期长', keyConstraints: '1.需在2026年8月前完成\n2.需与多部门协调\n3.规划需与国家省级规划衔接', deliverableRequirements: '规划文本20套、电子版5份、汇报PPT', approvalPath: '院内审核→专家咨询→市林业局审查→市发改委审核→市政府批准', hrAllocation: '项目负责人1人、产业分析师2人、调研工程师3人、规划编制2人', expectedOutputs: '1.产业现状调研报告\n2.产业规划文本\n3.项目清单及投资估算\n4.产业布局图', coreMaterials: '1.市统计年鉴\n2.林业产业统计年报\n3.土地利用现状数据', teamSetup: '以张主任为组长，9人团队，分调研组、编制组、综合组', coreStrategy: '1.深入产区调研\n2.对标国内外先进案例\n3.建立月度沟通机制', bidSituation: '竞争性谈判中标', procurementInfo: '攀枝花市林业局公开采购', acquisitionResult: '成功中标，合同金额85万元', projectImportance: '市林业系统年度重点工作', achievementDirection: '提交市林业局和市发改委备案' },
  { id: 4, name: '阿坝州草原生态修复实施方案', description: '阿坝州红原县、若尔盖县、阿坝县退化草原生态修复实施方案编制，涉及鼠荒地治理15万亩、人工种草8万亩、围栏封育20万亩。', status: 'active', createUserId: 2, createTime: '2026-06-01 08:00:00',
    customerLevel: 'A（重大项目）', contacts: '甲方：阿坝州林业和草原局草原科陈科长\n乙方：院草原生态研究所李主任', achievementType: '退化草原生态修复实施方案、工程布局图、投资概算表', approvalRequirements: '需通过州林草局审查、省林草局审批、州政府批准', canUndertake: '院草原生态研究所具有多年高寒草原研究经验，技术储备充分', mainRisks: '1.高寒地区施工窗口期短\n2.鼠荒地治理技术难度大\n3.草原监测需要长期跟踪', keyConstraints: '1.中央财政资金项目审计严格\n2.需与牧民协商草地使用权\n3.高海拔施工难度大', deliverableRequirements: '实施方案15套、电子版5份、工程布局图', approvalPath: '院内部三审→专家评审→州林草局审查→省林草局审批', hrAllocation: '项目负责人1人、草原生态工程师3人、畜牧工程师1人、GIS分析师1人', expectedOutputs: '1.草原退化调查报告\n2.生态修复实施方案\n3.工程布局图\n4.投资概算表', coreMaterials: '1.草原资源调查数据\n2.气象数据\n3.土壤类型分布图', teamSetup: '李主任为组长，8人团队，联合川农大草原研究所', coreStrategy: '1.遥感+地面调查结合\n2.分区域分类型制定方案\n3.向省林草局专题汇报', bidSituation: '公开招标中标，参与单位4家', procurementInfo: '阿坝州政府采购中心公开招标', acquisitionResult: '成功中标，合同金额180万元', projectImportance: '国家草原生态保护补助奖励政策配套项目', achievementDirection: '提交省林草局审批后作为工程实施依据' },
  { id: 5, name: '射洪市国土空间规划方案设计', description: '射洪市国土空间总体规划（2026-2035年）方案设计，涉及城市开发边界划定、生态保护红线评估、永久基本农田保护。', status: 'active', createUserId: 2, createTime: '2026-05-10 14:00:00',
    customerLevel: 'B（成长型项目）', contacts: '甲方：射洪市自然资源和规划局规划股王股长\n乙方：院国土空间规划所', achievementType: '国土空间总体规划方案、专题研究报告、图件集、数据库', approvalRequirements: '需通过市级专家评审、市政府常务会审议、报省自然资源厅审批', canUndertake: '规划专业齐全，院内具备城乡规划和土地规划双资质', mainRisks: '1.三区三线划定矛盾协调难度大\n2.市级部门意见反馈周期长', keyConstraints: '1.需在2026年底前完成\n2.需与遂宁市规划衔接', deliverableRequirements: '规划文本15套、专题报告10套、图件集、数据库光盘', approvalPath: '院审→专家评审→市规委会审议→市政府常务会→省自然资源厅审批', hrAllocation: '项目负责人1人、注册规划师2人、GIS分析师2人、交通工程师1人', expectedOutputs: '1.现状评估报告\n2.规划方案\n3.专题研究报告\n4.数据库', coreMaterials: '1.射洪市三调数据\n2.城规土规现有成果\n3.人口经济统计数据', teamSetup: '以国土空间规划所为班底，配置8人团队', coreStrategy: '1.双评价先行\n2.建立周报机制\n3.关键技术节点请院总工把关', bidSituation: '定向委托，射洪市为我院长期合作客户', procurementInfo: '射洪市自然资源和规划局直接委托', acquisitionResult: '已签订合同，合同金额95万元', projectImportance: '对我院在县级市场的品牌维护具有持续价值', achievementDirection: '纳入射洪市国土空间规划一张图系统' },
  { id: 6, name: '洪雅县航拍测绘及数据解算', description: '洪雅县全域航拍测绘项目，覆盖面积约1800km2，采用无人机倾斜摄影技术获取0.2m分辨率影像，进行正射校正、三维建模和数据解算。', status: 'active', createUserId: 2, createTime: '2026-04-01 09:00:00',
    customerLevel: 'B（成长型项目）', contacts: '甲方：洪雅县测绘地理信息局技术科周科长\n乙方：院测绘与遥感所', achievementType: 'DOM正射影像图、DEM数字高程模型、三维实景模型、控制点成果表', approvalRequirements: '需通过县测绘地理信息局验收、省级测绘产品质量检验', canUndertake: '院测绘与遥感所具备甲级测绘资质和无人机航测能力', mainRisks: '1.夏秋季多雨雾影响航拍\n2.山区地形复杂像控点布设困难\n3.数据处理量大周期长', keyConstraints: '1.需在3个月内完成\n2.航拍需避开雨季\n3.精度满足1:2000测图要求', deliverableRequirements: 'DOM正射影像、DEM、三维实景模型、控制点成果表', approvalPath: '院内部质检→县测绘局验收→省级质检站抽检', hrAllocation: '项目负责人1人、航拍飞手2人、像控测量3人、内业处理3人、质检1人', expectedOutputs: '1.控制点成果表\n2.DOM正射影像图\n3.DEM数字高程模型\n4.三维实景模型', coreMaterials: '1.行政区划图\n2.已有地形图资料\n3.卫星影像参考数据', teamSetup: '以测绘与遥感所为主，10人团队，分航飞组、像控组、内业组', coreStrategy: '1.利用现有像控点减少外业\n2.多机组并行作业\n3.GPU集群加速处理', bidSituation: '竞争性谈判中标，参与单位3家', procurementInfo: '洪雅县政府采购中心公开采购', acquisitionResult: '成功中标，合同金额156万元', projectImportance: '洪雅县数字城市建设的基础性项目', achievementDirection: '提交县测绘局入库，作为城市规划基础底图' },
  { id: 7, name: '彭州市林地资源调查实施', description: '彭州市全域林地资源调查项目，涉及20个乡镇的林种、树种、龄组、蓄积量、森林覆盖率等指标调查，设置固定样地800个。', status: 'active', createUserId: 2, createTime: '2026-06-01 10:00:00',
    customerLevel: 'B（成长型项目）', contacts: '甲方：彭州市林业和园林管理局资源科何科长\n乙方：院森林资源监测所', achievementType: '林地资源调查报告、样地调查表汇编、森林资源分布图、蓄积量统计表', approvalRequirements: '需通过市林业和园林管理局验收', canUndertake: '院森林资源监测所长期从事森林资源调查监测工作', mainRisks: '1.林地分散交通不便\n2.样地数量多外业周期长\n3.部分林区存在权属争议', keyConstraints: '1.需在5个月内完成\n2.精度满足国家森林资源清查标准\n3.数据需与省监测平台对接', deliverableRequirements: '调查报告15套、样地调查表汇编3套、图件集、电子数据光盘5份', approvalPath: '院内质检→市林业和园林管理局验收', hrAllocation: '项目负责人1人、林业调查工程师5人、数据处理2人、GIS制图1人、质检1人', expectedOutputs: '1.样地调查表汇编\n2.林地资源调查报告\n3.森林资源分布图\n4.蓄积量统计表', coreMaterials: '1.二类调查历史数据\n2.林地一张图数据\n3.国土三调数据', teamSetup: '以森林资源监测所为主，10人团队，分5个外业组，内业组3人', coreStrategy: '1.按乡镇分区同步推进\n2.平板电脑野外数据采集\n3.数据当日审核机制', bidSituation: '公开招标中标，参与单位3家', procurementInfo: '彭州市政府采购中心公开招标', acquisitionResult: '成功中标，合同金额98万元', projectImportance: '市森林资源动态监测体系的重要组成部分', achievementDirection: '提交市林业和园林管理局，数据纳入省监测平台' },
  { id: 8, name: '绵竹市外业调查与数据分析', description: '绵竹市全域林业外业调查与资源数据分析，重点对龙门山脉区域森林资源（含大熊猫栖息地）进行系统调查，涉及天然林保护成效评估、退耕还林产业调查、森林碳汇潜力评估。', status: 'active', createUserId: 2, createTime: '2026-05-01 08:00:00',
    customerLevel: 'C（一般项目）', contacts: '甲方：绵竹市自然资源局林业股赵股长\n乙方：院森林资源监测所', achievementType: '林业调查报告、天然林保护成效评估报告、退耕还林产业调查报告、碳汇评估报告', approvalRequirements: '需通过市自然资源局验收', canUndertake: '项目规模适中，院内技术能力充分', mainRisks: '1.龙门山区域地形险峻\n2.大熊猫栖息地调查受限\n3.碳汇评估方法学要求高', keyConstraints: '1.需在3个月内完成\n2.龙门山区需配备登山装备和向导\n3.碳汇评估需参考最新方法学', deliverableRequirements: '各类报告共10套、数据光盘5份', approvalPath: '院内审核→市自然资源局验收', hrAllocation: '项目负责人1人、林业调查工程师3人、数据分析师1人、碳汇专家1人', expectedOutputs: '1.林业调查报告\n2.天然林保护成效评估\n3.退耕还林产业调查\n4.森林碳汇评估', coreMaterials: '1.森林资源档案\n2.天然林保护实施方案\n3.退耕还林验收资料', teamSetup: '以森林资源监测所为主，6人团队，含1名碳汇评估外部专家', coreStrategy: '1.外业安排在天气稳定的6-8月\n2.聘请当地向导\n3.碳汇评估与川大合作', bidSituation: '询价采购中标', procurementInfo: '绵竹市自然资源局询价采购', acquisitionResult: '中标，合同金额45万元', projectImportance: '年度常规林业监测项目，对维护客户关系和锻炼团队有持续价值', achievementDirection: '调查数据和分析报告提交市自然资源局存档' }
]

const projectMembers = {
  1: [
    { id: 1, userId: 2, realName: '张主任', dept: '规划一室', roleInProject: 'manager', status: 'confirmed' },
    { id: 2, userId: 3, realName: '李工', dept: '规划一室', roleInProject: 'engineer', status: 'confirmed' }
  ],
  2: [
    { id: 3, userId: 2, realName: '张主任', dept: '规划一室', roleInProject: 'manager', status: 'confirmed' },
    { id: 4, userId: 3, realName: '李工', dept: '规划一室', roleInProject: 'engineer', status: 'confirmed' }
  ],
  3: [
    { id: 5, userId: 2, realName: '张主任', dept: '规划一室', roleInProject: 'manager', status: 'confirmed' },
    { id: 6, userId: 3, realName: '李工', dept: '规划一室', roleInProject: 'engineer', status: 'confirmed' }
  ]
}

const stages = {
  1: [
    { id: 1, projectId: 1, stageName: '外业调查', description: '古蔺县26个乡镇森林资源外业调查：样地布设与编号、树种识别与每木检尺、蓄积量测算、林分因子及立地条件采集', planStart: '2026-05-15', planEnd: '2026-06-15', actualStart: '2026-05-16', actualEnd: null, status: 'submitted', assigneeId: 3, assigneeName: '李工', sortOrder: 0, latestReport: { id: 1, content: '已完成南部片区调查约15万亩设样地320个树种马尾松杉木柏木', progressRate: 65, problem: '北部片区地形复杂交通不便预计延迟5天', reviewStatus: 'pending' } },
    { id: 2, projectId: 1, stageName: '内业整理', description: '外业数据汇总整理、样地调查表审核与录入、树种分布图绘制、森林蓄积量统计与生长量分析', planStart: '2026-06-16', planEnd: '2026-07-10', actualStart: null, actualEnd: null, status: 'pending', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: null },
    { id: 3, projectId: 1, stageName: '成果提交', description: '编制林地保护利用规划文本（含说明书和图件集），组织院内部三级审核后提交县自然资源局初审', planStart: '2026-07-11', planEnd: '2026-07-31', actualStart: null, actualEnd: null, status: 'pending', assigneeId: 3, assigneeName: '李工', sortOrder: 2, latestReport: null }
  ],
  2: [
    { id: 4, projectId: 2, stageName: '需求调研', description: '与各部门沟通需求梳理功能清单', planStart: '2026-03-01', planEnd: '2026-03-15', actualStart: '2026-03-02', actualEnd: '2026-03-14', status: 'completed', assigneeId: 3, assigneeName: '李工', sortOrder: 0, latestReport: { id: 4, content: '完成需求调研梳理5大模块23项功能形成需求文档V1.0', progressRate: 100, problem: '', reviewStatus: 'passed' } },
    { id: 5, projectId: 2, stageName: '系统开发', description: '基于SpringBoot+Vue3全栈开发：MySQL数据库设计与30+表搭建、RESTful API接口开发与Swagger文档、Element Plus前端页面开发与前后端联调', planStart: '2026-03-16', planEnd: '2026-04-20', actualStart: '2026-03-18', actualEnd: '2026-04-25', status: 'completed', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: { id: 5, content: '完成V0.1至V0.6版本开发含30多后端文件15前端页面', progressRate: 100, problem: 'Mock与后端URL不一致已修复', reviewStatus: 'passed' } },
    { id: 6, projectId: 2, stageName: '测试上线', description: '功能测试部署上线', planStart: '2026-04-26', planEnd: '2026-05-10', actualStart: '2026-04-28', actualEnd: '2026-05-12', status: 'completed', assigneeId: 3, assigneeName: '李工', sortOrder: 2, latestReport: { id: 6, content: '全流程测试修复12个bug已部署Netlify演示版', progressRate: 100, problem: '移动端显示效果需优化', reviewStatus: 'passed' } }
  ],
  3: [
    { id: 7, projectId: 3, stageName: '前期调研', description: '攀枝花市域林业产业现状调研', planStart: '2026-05-20', planEnd: '2026-06-10', actualStart: '2026-05-21', actualEnd: '2026-06-08', status: 'completed', assigneeId: 3, assigneeName: '李工', sortOrder: 0, latestReport: { id: 7, content: '完成仁和区米易县盐边县3区县调研走访企业15家', progressRate: 100, problem: '米易县部分乡镇交通不便', reviewStatus: 'passed' } },
    { id: 8, projectId: 3, stageName: '规划编制', description: '编写产业发展规划文本及说明', planStart: '2026-06-11', planEnd: '2026-07-15', actualStart: '2026-06-12', actualEnd: null, status: 'submitted', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: { id: 8, content: '完成规划初稿编制围绕特色经济林林下经济森林康养三大方向', progressRate: 70, problem: '林下经济方向数据支撑不足需补充调研', reviewStatus: 'pending' } },
    { id: 9, projectId: 3, stageName: '成果评审', description: '组织专家评审修改完善报批', planStart: '2026-07-16', planEnd: '2026-07-31', actualStart: null, actualEnd: null, status: 'pending', assigneeId: 3, assigneeName: '李工', sortOrder: 2, latestReport: null }
  ],
  4: [
    { id: 10, projectId: 4, stageName: '外业调查', description: '红原、若尔盖、阿坝三县退化草原调查：高原鼠兔和鼢鼠危害等级评定、植被群落样方调查、土壤理化性质采样分析', planStart: '2026-06-01', planEnd: '2026-07-01', actualStart: '2026-06-03', actualEnd: null, status: 'in_progress', assigneeId: 3, assigneeName: '李工', sortOrder: 0, latestReport: { id: 10, content: '完成红原县若尔盖县草原调查布设样方200个采集土壤80份', progressRate: 55, problem: '7月上旬红原县连续出现雷雨和冰雹天气，草原泥泞车辆无法进入样地，实际工作日仅完成计划的40%，鼠荒地调查进度严重滞后', reviewStatus: 'pending' } },
    { id: 11, projectId: 4, stageName: '方案编制', description: '编制退化草原生态修复实施方案：鼠荒地分区治理设计、人工种草品种选配与播种方案、围栏封育布局规划及投资概算', planStart: '2026-07-02', planEnd: '2026-07-25', actualStart: null, actualEnd: null, status: 'pending', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: null }
  ],
  5: [
    { id: 12, projectId: 5, stageName: '数据收集', description: '收集自然资源交通水利等基础数据', planStart: '2026-05-10', planEnd: '2026-05-25', actualStart: '2026-05-11', actualEnd: '2026-05-24', status: 'completed', assigneeId: 3, assigneeName: '李工', sortOrder: 0, latestReport: { id: 12, content: '完成基础数据收集含国土三调数据林地一张图', progressRate: 100, problem: '部分乡镇数据更新不及时', reviewStatus: 'passed' } },
    { id: 13, projectId: 5, stageName: '方案设计', description: '编制项目总体方案及技术路线', planStart: '2026-05-26', planEnd: '2026-06-15', actualStart: '2026-05-27', actualEnd: null, status: 'submitted', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: { id: 13, content: '完成方案框架设计确定技术路线采用遥感加地面调查结合', progressRate: 50, problem: '遥感数据采购预算超出预期', reviewStatus: 'pending' } }
  ],
  6: [
    { id: 14, projectId: 6, stageName: '外业作业', description: '无人机倾斜摄影外业：像控点布设与GNSS-RTK测量、分区航线规划与五镜头航拍数据采集、飞行质量现场检查', planStart: '2026-04-01', planEnd: '2026-04-30', actualStart: '2026-04-03', actualEnd: '2026-05-05', status: 'completed', assigneeId: 3, assigneeName: '李工', sortOrder: 0, latestReport: { id: 14, content: '完成全部外业作业航拍面积50km2标志点埋设120个', progressRate: 100, problem: '4月中旬连续降雨延误5天', reviewStatus: 'passed' } },
    { id: 15, projectId: 6, stageName: '内业处理', description: '内业数据处理：影像匀色与空中三角测量、密集点云生成与三维实景建模、DOM正射影像和DEM数字高程模型编制与精度评定', planStart: '2026-05-06', planEnd: '2026-05-31', actualStart: '2026-05-08', actualEnd: null, status: 'in_progress', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: { id: 15, content: '完成影像拼接和正射校正正在进行数据解算', progressRate: 60, problem: '部分航拍区域影像拼接存在色差', reviewStatus: 'pending' } }
  ],
  7: [
    { id: 16, projectId: 7, stageName: '现场踏勘', description: '项目区现场踏勘了解地形地貌植被分布', planStart: '2026-06-01', planEnd: '2026-06-10', actualStart: '2026-06-02', actualEnd: '2026-06-09', status: 'completed', assigneeId: 3, assigneeName: '李工', sortOrder: 0, latestReport: { id: 16, content: '完成项目区踏勘了解地形变化植被分布现状确定调查重点区域', progressRate: 100, problem: '', reviewStatus: 'passed' } },
    { id: 17, projectId: 7, stageName: '调查实施', description: '样地设置树种识别蓄积量调查', planStart: '2026-06-11', planEnd: '2026-07-10', actualStart: null, actualEnd: null, status: 'pending', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: null }
  ],
  8: [
    { id: 18, projectId: 8, stageName: '前期准备', description: '资料收集设备调试人员培训', planStart: '2026-05-01', planEnd: '2026-05-15', actualStart: '2026-05-02', actualEnd: '2026-05-14', status: 'completed', assigneeId: 3, assigneeName: '李工', sortOrder: 0, latestReport: { id: 18, content: '完成资料收集设备调试和人员培训准备就绪', progressRate: 100, problem: '', reviewStatus: 'passed' } },
    { id: 19, projectId: 8, stageName: '正式调查', description: '全面开展外业调查工作', planStart: '2026-05-16', planEnd: '2026-06-30', actualStart: '2026-05-18', actualEnd: null, status: 'in_progress', assigneeId: 3, assigneeName: '李工', sortOrder: 1, latestReport: { id: 19, content: '完成东部片区调查约8万亩开始西部片区', progressRate: 45, problem: '西部片区地形复杂调查进度偏慢', reviewStatus: 'pending' } }
  ]
}

let reportIdCounter = 100
const reports = {
  1: [{ id: 1, stageId: 1, projectId: 1, content: '已完成南部片区调查约15万亩设样地320个树种马尾松杉木柏木', progressRate: 65, problem: '北部片区地形复杂交通不便预计延迟5天', qualityControl: '每样地8个检尺点数据当日复核', resultSummary: '完成调查表120份树种分布图2幅', coordinationNote: '与县林业局对接3次院内部协调会2次', actualStart: '2026-05-16', actualEnd: null, reviewStatus: 'pending', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-05-28 16:30:00', reviewUserId: null, reviewTime: null, createTime: '2026-05-28 16:30:00', attachmentName: '调查报告初稿.pdf', stageName: '外业调查', projectName: '古蔺县森林资源调查与空间规划' }],
  4: [{ id: 4, stageId: 4, projectId: 2, content: '完成需求调研梳理5大模块23项功能形成需求文档V1.0', progressRate: 100, problem: '', qualityControl: '每项需求与业务部门确认签字', resultSummary: '需求规格说明书1份', coordinationNote: '', actualStart: '2026-03-02', actualEnd: null, reviewStatus: 'passed', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-03-14 10:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-03-14 10:00:00', attachmentName: null, stageName: '需求调研', projectName: '规划院项目全过程管理系统' }],
  5: [{ id: 5, stageId: 5, projectId: 2, content: '完成V0.1至V0.6版本开发含30多后端文件15前端页面', progressRate: 100, problem: 'Mock与后端URL不一致已修复', qualityControl: '每版本经2轮审查', resultSummary: '系统前后端完整代码', coordinationNote: '', actualStart: '2026-03-18', actualEnd: null, reviewStatus: 'passed', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-04-20 14:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-04-20 14:00:00', attachmentName: null, stageName: '系统开发', projectName: '规划院项目全过程管理系统' }],
  6: [{ id: 6, stageId: 6, projectId: 2, content: '全流程测试修复12个bug已部署Netlify演示版', progressRate: 100, problem: '移动端显示效果需优化', qualityControl: '测试覆盖全部业务场景', resultSummary: '测试报告1份', coordinationNote: '', actualStart: '2026-04-28', actualEnd: null, reviewStatus: 'passed', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-05-10 16:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-05-10 16:00:00', attachmentName: null, stageName: '测试上线', projectName: '规划院项目全过程管理系统' }],
  7: [{ id: 7, stageId: 7, projectId: 3, content: '完成仁和区米易县盐边县3区县调研走访企业15家收集数据120份', progressRate: 100, problem: '米易县部分乡镇交通不便', qualityControl: '每区县2人一组交叉验证数据', resultSummary: '调研报告1份数据汇总表1套', coordinationNote: '与市林业局对接4次', actualStart: '2026-05-21', actualEnd: null, reviewStatus: 'passed', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-06-08 11:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-06-08 11:00:00', attachmentName: null, stageName: '前期调研', projectName: '攀枝花市林业产业发展规划' }],
  8: [{ id: 8, stageId: 8, projectId: 3, content: '完成规划初稿编制围绕特色经济林林下经济森林康养三大方向', progressRate: 70, problem: '林下经济方向数据支撑不足需补充调研', qualityControl: '引用数据标注来源经专家咨询确认', resultSummary: '规划文本初稿1份附件3份', coordinationNote: '与院技术委员会汇报1次', actualStart: '2026-06-12', actualEnd: null, reviewStatus: 'pending', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-06-28 09:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-06-28 09:00:00', attachmentName: '攀枝花林业产业规划初稿.pdf', stageName: '规划编制', projectName: '攀枝花市林业产业发展规划' }],
  10: [{ id: 10, stageId: 10, projectId: 4, content: '完成红原县若尔盖县草原调查布设样方200个采集土壤样本80份', progressRate: 55, problem: '7月上旬红原县连续出现雷雨和冰雹天气，草原泥泞车辆无法进入样地，实际工作日仅完成计划的40%，鼠荒地调查进度严重滞后', qualityControl: '样方GPS定位精度小于5m', resultSummary: '样方调查表200份土壤样本80份', coordinationNote: '与县草原站协调2次', actualStart: '2026-06-03', actualEnd: null, reviewStatus: 'pending', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-06-20 15:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-06-20 15:00:00', attachmentName: null, stageName: '外业调查', projectName: '阿坝州草原生态修复实施方案' }],
  12: [{ id: 12, stageId: 12, projectId: 5, content: '完成基础数据收集含国土三调数据林地一张图交通水利分布图', progressRate: 100, problem: '部分乡镇数据更新不及时', qualityControl: '数据来源标注清晰多方交叉验证', resultSummary: '基础数据集1套', coordinationNote: '与县自然资源局对接1次', actualStart: '2026-05-11', actualEnd: null, reviewStatus: 'passed', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-05-24 09:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-05-24 09:00:00', attachmentName: null, stageName: '数据收集', projectName: '射洪市国土空间规划方案设计' }],
  13: [{ id: 13, stageId: 13, projectId: 5, content: '完成方案框架设计确定技术路线采用遥感加地面调查结合方式', progressRate: 50, problem: '遥感数据采购预算超出预期', qualityControl: '技术路线参考同类项目经验', resultSummary: '方案框架及技术路线说明1份', coordinationNote: '与院总工办技术讨论1次', actualStart: '2026-05-27', actualEnd: null, reviewStatus: 'pending', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-06-10 14:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-06-10 14:00:00', attachmentName: null, stageName: '方案设计', projectName: '射洪市国土空间规划方案设计' }],
  14: [{ id: 14, stageId: 14, projectId: 6, content: '完成全部外业作业航拍面积50km2标志点埋设120个', progressRate: 100, problem: '4月中旬连续降雨延误5天', qualityControl: '航拍重叠率大于60%标志点复核率100%', resultSummary: '航拍影像50km2控制点成果表', coordinationNote: '与测绘大队协调2次', actualStart: '2026-04-03', actualEnd: null, reviewStatus: 'passed', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-05-05 16:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-05-05 16:00:00', attachmentName: null, stageName: '外业作业', projectName: '洪雅县航拍测绘及数据解算' }],
  15: [{ id: 15, stageId: 15, projectId: 6, content: '完成影像拼接和正射校正正在进行数据解算', progressRate: 60, problem: '部分航拍区域影像拼接存在色差', qualityControl: '影像分辨率优于0.2m', resultSummary: '正射影像图1幅', coordinationNote: '', actualStart: '2026-05-08', actualEnd: null, reviewStatus: 'pending', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-05-25 10:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-05-25 10:00:00', attachmentName: null, stageName: '内业处理', projectName: '洪雅县航拍测绘及数据解算' }],
  16: [{ id: 16, stageId: 16, projectId: 7, content: '完成项目区踏勘了解地形变化植被分布现状确定调查重点区域', progressRate: 100, problem: '', qualityControl: '踏勘路线覆盖项目区80%以上', resultSummary: '踏勘报告1份', coordinationNote: '', actualStart: '2026-06-02', actualEnd: null, reviewStatus: 'passed', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-06-09 11:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-06-09 11:00:00', attachmentName: null, stageName: '现场踏勘', projectName: '彭州市林地资源调查实施' }],
  18: [{ id: 18, stageId: 18, projectId: 8, content: '完成资料收集设备调试和人员培训准备就绪', progressRate: 100, problem: '', qualityControl: '培训覆盖率100%', resultSummary: '培训记录及设备清单', coordinationNote: '', actualStart: '2026-05-02', actualEnd: null, reviewStatus: 'passed', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-05-14 09:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-05-14 09:00:00', attachmentName: null, stageName: '前期准备', projectName: '绵竹市外业调查与数据分析' }],
  19: [{ id: 19, stageId: 19, projectId: 8, content: '完成东部片区调查约8万亩开始西部片区', progressRate: 45, problem: '西部片区地形复杂调查进度偏慢', qualityControl: '每样地数据当日录入当日复核', resultSummary: '东部片区调查数据', coordinationNote: '', actualStart: '2026-05-18', actualEnd: null, reviewStatus: 'pending', reviewComment: null, submitUserId: 3, submitUserName: '李工', submitTime: '2026-06-10 14:00:00', reviewUserId: null, reviewTime: null, createTime: '2026-06-10 14:00:00', attachmentName: null, stageName: '正式调查', projectName: '绵竹市外业调查与数据分析' }]
}

const deviations = [
  { id: 1, projectId: 1, stageId: 1, reportId: 1, type: 'auto', description: '北部黄荆老林片区地形复杂致外业延迟5天', reason: '黄荆老林片区海拔落差大、灌丛密布，样地布设和测量耗时远超预期，部分样地需绕行3公里以上', impact: '外业调查完工时间从6月15日延至6月20日，后续内业整理和成果提交顺延', status: 'open', createUserId: 2, createUserName: '张主任', createTime: '2026-05-28 17:00:00', closeTime: null, projectName: '古蔺县森林资源调查与空间规划', stageName: '外业调查' },
  { id: 2, projectId: 3, stageId: 8, reportId: 8, type: 'auto', description: '米易县林下食用菌和中药材产业数据严重缺失致编制延迟3天', reason: '甲方项目对接人因岗位调整已调离，新对接人不熟悉情况，米易县近三年林下经济产量和产值数据无法按时提供', impact: '产业规划编制进度顺延3天，如本周内数据仍不到位可能扩大至5天', status: 'open', createUserId: 2, createUserName: '张主任', createTime: '2026-06-28 09:00:00', closeTime: null, projectName: '攀枝花市林业产业发展规划', stageName: '规划编制' },
  { id: 3, projectId: 6, stageId: 14, reportId: 14, type: 'auto', description: '4月中旬连续降雨导致无人机航拍延期5天', reason: '4月12日至16日连续5天中到大雨，山区能见度不足500米且云层低于200米，大疆M300无人机无法安全起飞，航拍作业完全停滞', impact: '外业作业从4月30日延至5月5日完工，内业处理启动时间同步顺延，整体项目工期面临压缩压力', status: 'open', createUserId: 2, createUserName: '张主任', createTime: '2026-05-05 16:00:00', closeTime: null, projectName: '洪雅县航拍测绘及数据解算', stageName: '外业作业' },
  { id: 4, projectId: 8, stageId: 19, reportId: 19, type: 'auto', description: '西部龙门山镇片区地形陡峭调查进度落后3-5天', reason: '龙门山镇坡度普遍在35度以上，样地到达时间平均1.5小时，且部分区域属于大熊猫栖息地核心区需额外审批，日均完成样地数仅为东部片区的40%', impact: '如不增派人员，正式调查阶段将延迟3-5天，可能挤压后续数据分析工期', status: 'open', createUserId: 2, createUserName: '张主任', createTime: '2026-06-10 14:00:00', closeTime: null, projectName: '绵竹市外业调查与数据分析', stageName: '正式调查' }
]

const supportItems = [
  { id: 1, projectId: 1, title: '请院领导发函协调古蔺县林业局提供北部片区林地权属资料', content: '黄荆老林片区涉及3个乡镇约8万亩林地，外业调查需要县林业局提供林地权属界线图和林权证台账。目前与县林业局林政股沟通3次均无实质进展，对方以"涉及林农隐私和权属争议"为由拒绝提供电子数据，仅允许现场查阅。没有权属数据，样地成果无法落地上图，建议院级层面出具正式函件并请院领导电话协调。', applicantId: 3, applicantName: '李工', handlerId: 2, handlerName: '张主任', expectTime: '2026-06-05', status: 'pending', reply: null, resolveNote: null, createTime: '2026-05-28 17:30:00', updateTime: '2026-05-28 17:30:00', projectName: '古蔺县森林资源调查与空间规划' },
  { id: 2, projectId: 4, title: '申请院级经费邀请川农大草原专家参与鼠荒地治理方案评审', content: '阿坝州红原县鼠荒地治理涉及高原鼢鼠和黑唇鼠兔两个优势种的生物防治方案设计，院内草原生态专业力量不足，需要邀请四川农业大学草原研究所周教授团队参与方案评审和技术指导。预计需专家费1.5万元（含差旅），请院领导审批经费并协调专家时间，期望在6月15日前完成评审。', applicantId: 3, applicantName: '李工', handlerId: 2, handlerName: '张主任', expectTime: '2026-06-15', status: 'pending', reply: null, resolveNote: null, createTime: '2026-05-26 14:00:00', updateTime: '2026-05-26 14:00:00', projectName: '阿坝州草原生态修复项目实施方案' }
]

const changes = {}

const approvals = {}

const reviews = {
  1: { id: 1, projectId: 1, overallDeviation: '外业偏差15天，内业偏差30天，成果偏差30天。前期对工作量预估不足，导致多次延期。', efficiencyRating: '整体效率偏低。前期对地形复杂度和协调难度预估不足，外业阶段雨天停工较多，内业阶段因数据不完整多次返工。建议后续类似项目增加15%的时间buffer。', qualityRating: '成果质量整体满足要求，但图件标注存在少量错误，报告文字有少量纰漏。经专家审核后已修正，最终通过评审。外业数据采集较为扎实，为后续分析提供了良好基础。', communicationNote: '与业主的沟通总体顺畅，关键节点均及时汇报。但在外业调查阶段与地方林业站的协调不够，导致资料获取延迟。院级层面介入后问题得到解决。后续建议在项目启动期就建立多方沟通机制。', createUserId: 2, createTime: '2026-07-25 10:00:00', projectName: '古蔺县森林资源调查与空间规划' }
}

const experiences = {
  1: { id: 1, projectId: 1, reusableExperience: '1. 古蔺项目的外业调查分组模式值得复用——按地形单元分4个小组并行推进，每组配置林业+测绘+地理专业人员，效率提升明显。\n2. 建立了完整的林地资源数据库模板，包括样地调查表、树种分布图、蓄积量统计表等，可直接复用于类似项目。\n3. 与业主的定期沟通机制（每周五下午进度通报）有效减少了后期返工。', shortcomings: '1. 前期对地形复杂度评估不足，北部山区交通条件差，导致外业延迟。\n2. 内业阶段数据分析人才配置不足，过度依赖1-2名骨干，形成瓶颈。\n3. 缺乏标准化的质量审核流程，第一版成果内部审核不够严格。', risks: '1. 地形复杂区域的外业调查时间窗口受天气影响大，需预留弹性时间。\n2. 地方数据获取存在行政壁垒，需要提前建立协调机制。\n3. 成果编制阶段多专业协作容易出现衔接遗漏。', improvement: '1. 建议在项目启动前进行实地踏勘，准确评估调查难度和时间。\n2. 建立内部三级审核制度（自审→互审→专家审），提高成果质量。\n3. 推广古蔺项目的分组并行作业模式到其他大型调查项目。\n4. 加强与地方林业部门的常态化联系，降低数据获取成本。', createUserId: 2, createTime: '2026-07-25 11:00:00', projectName: '古蔺县森林资源调查与空间规划' }
}

export function getMockData() {
  return { users, projects, projectMembers, stages, reports, deviations, supportItems, changes, approvals, reviews, experiences, currentUser }
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
