-- 测试数据填充脚本
USE pm_system;

-- 清理旧数据
DELETE FROM sys_deviation;
DELETE FROM sys_stage_report;
DELETE FROM sys_project_stage;

-- ==================== 项目1: 古蔺县森林资源调查(已有) ====================
UPDATE sys_project SET status='active' WHERE id=1;

INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES
(1,'外业调查','实地勘测数据采集样地调查','2026-05-15','2026-06-15','2026-05-16',NULL,'submitted',3,0),
(1,'内业整理','数据整理图纸绘制统计分析','2026-06-16','2026-07-10',NULL,NULL,'pending',3,1),
(1,'成果提交','编制报告制作图件提交审核','2026-07-11','2026-07-31',NULL,NULL,'pending',3,2);

INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time,attachment_name) VALUES
(1,1,'已完成南部片区调查约15万亩设样地320个树种马尾松杉木柏木蓄积量85万m3',65,'北部片区地形复杂交通不便预计延迟5天','每样地8个检尺点数据当日复核','完成调查表120份树种分布图2幅','与县林业局对接3次院内部协调会2次','2026-05-16','pending',3,'2026-05-28 16:30:00','调查报告初稿.pdf');

INSERT INTO sys_deviation (project_id,stage_id,report_id,type,description,reason,impact,status,create_user_id) VALUES
(1,1,1,'auto','北部片区地形复杂延迟5天','地形复杂部分区域交通不便','工期延后5天','open',2);

-- ==================== 项目2: 项目管理系统 (已完成项目) ====================
UPDATE sys_project SET status='completed' WHERE id=2;

INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES
(2,'需求调研','与各部门沟通需求梳理功能清单','2026-03-01','2026-03-15','2026-03-02','2026-03-14','completed',3,0),
(2,'系统开发','前后端开发数据库联调测试','2026-03-16','2026-04-20','2026-03-18','2026-04-25','completed',3,1),
(2,'测试上线','功能测试部署上线','2026-04-26','2026-05-10','2026-04-28','2026-05-12','completed',3,2);

INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,review_status,submit_user_id,submit_time) VALUES
(4,2,'完成需求调研梳理5大模块23项功能形成需求文档V1.0',100,'','每项需求与业务部门确认签字','需求规格说明书1份','passed',3,'2026-03-14 10:00:00'),
(5,2,'完成V0.1至V0.6版本开发含30多后端文件15前端页面7张数据表',100,'Mock模式与真后端URL不一致已修复','每版本经2轮审查','系统前后端完整代码','passed',3,'2026-04-20 14:00:00'),
(6,2,'完成全流程测试修复12个bug已部署Netlify演示版',100,'移动端显示效果需优化','测试覆盖全部业务场景','测试报告1份','passed',3,'2026-05-10 16:00:00');

-- ==================== 项目3: 攀枝花林业规划 (active, 有偏差) ====================
UPDATE sys_project SET status='active' WHERE id=3;

INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES
(3,'前期调研','攀枝花市域林业产业现状调研','2026-05-20','2026-06-10','2026-05-21','2026-06-08','completed',3,0),
(3,'规划编制','编写产业发展规划文本及说明','2026-06-11','2026-07-15','2026-06-12',NULL,'submitted',3,1),
(3,'成果评审','组织专家评审修改完善报批','2026-07-16','2026-07-31',NULL,NULL,'pending',3,2);

INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,review_status,submit_user_id,submit_time,attachment_name) VALUES
(7,3,'完成仁和区米易县盐边县3区县调研走访企业15家收集产业数据120份',100,'米易县部分乡镇交通不便','每区县2人一组交叉验证数据','调研报告1份数据汇总表1套','与市林业局对接4次','passed',3,'2026-06-08 11:00:00',NULL),
(8,3,'完成规划初稿编制围绕特色经济林林下经济森林康养三大方向',70,'林下经济方向数据支撑不足需补充调研','引用数据标注来源经专家咨询确认','规划文本初稿1份附件3份','与院技术委员会汇报1次','pending',3,'2026-06-28 09:00:00','攀枝花林业产业规划初稿.pdf');

INSERT INTO sys_deviation (project_id,stage_id,report_id,type,description,reason,impact,status,create_user_id) VALUES
(3,8,8,'auto','林下经济数据不足导致编制延迟3天','项目对接人变更部分数据缺失','编制时间顺延3天','open',2);

-- ==================== 项目4: 草原生态修复 ====================
UPDATE sys_project SET status='active' WHERE id=4;

INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES
(4,'外业调查','草原退化现状调查鼠荒地治理评估','2026-06-01','2026-07-01','2026-06-03',NULL,'in_progress',3,0),
(4,'方案编制','退化草原生态修复实施方案编制','2026-07-02','2026-07-25',NULL,NULL,'pending',3,1);

INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,review_status,submit_user_id,submit_time) VALUES
(9,4,'完成红原县若尔盖县草原调查布设样方200个采集土壤样本80份',55,'高寒地区天气多变调查进度受降雨影响','样方GPS定位精度小于5m','样方调查表200份土壤样本80份','与县草原站协调2次','pending',3,'2026-06-20 15:00:00');

-- ==================== 项目5: 方案设计项目 ====================
UPDATE sys_project SET status='active' WHERE id=5;

INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES
(5,'数据收集','收集自然资源交通水利等基础数据','2026-05-10','2026-05-25','2026-05-11','2026-05-24','completed',3,0),
(5,'方案设计','编制项目总体方案及技术路线','2026-05-26','2026-06-15','2026-05-27',NULL,'submitted',3,1);

INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,review_status,submit_user_id,submit_time) VALUES
(11,5,'完成基础数据收集含国土三调数据林地一张图交通水利分布图',100,'部分乡镇数据更新不及时','数据来源标注清晰多方交叉验证','基础数据集1套','与县自然资源局对接1次','passed',3,'2026-05-24 09:00:00'),
(12,5,'完成方案框架设计确定技术路线采用遥感加地面调查结合方式',50,'遥感数据采购预算超出预期','技术路线参考同类项目经验','方案框架及技术路线说明1份','与院总工办技术讨论1次','pending',3,'2026-06-10 14:00:00');

-- ==================== 项目6: 航拍测绘项目 (有偏差) ====================
UPDATE sys_project SET status='active' WHERE id=6;

INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES
(6,'外业作业','实地测量标志点埋设航拍数据采集','2026-04-01','2026-04-30','2026-04-03','2026-05-05','completed',3,0),
(6,'内业处理','影像处理数据解算成果编制','2026-05-06','2026-05-31','2026-05-08',NULL,'in_progress',3,1);

INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,review_status,submit_user_id,submit_time) VALUES
(13,6,'完成全部外业作业航拍面积50km2标志点埋设120个',100,'4月中旬连续降雨延误5天','航拍重叠率大于60%标志点复核率100%','航拍影像50km2控制点成果表','与测绘大队协调2次','passed',3,'2026-05-05 16:00:00'),
(14,6,'完成影像拼接和正射校正正在进行数据解算',60,'部分航拍区域影像拼接存在色差','影像分辨率优于0.2m','正射影像图1幅','','pending',3,'2026-05-25 10:00:00');

INSERT INTO sys_deviation (project_id,stage_id,report_id,type,description,reason,impact,status,create_user_id) VALUES
(6,13,13,'auto','连续降雨导致外业延期5天','4月中旬连续降雨无法作业','整体工期延后5天','open',2);

-- ==================== 项目7: 调查实施项目 ====================
UPDATE sys_project SET status='active' WHERE id=7;

INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES
(7,'现场踏勘','项目区现场踏勘了解地形地貌植被分布','2026-06-01','2026-06-10','2026-06-02','2026-06-09','completed',3,0),
(7,'调查实施','样地设置树种识别蓄积量调查','2026-06-11','2026-07-10',NULL,NULL,'pending',3,1);

INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,review_status,submit_user_id,submit_time) VALUES
(15,7,'完成项目区踏勘了解地形变化植被分布现状确定调查重点区域',100,'','踏勘路线覆盖项目区80%以上','踏勘报告1份','passed',3,'2026-06-09 11:00:00');

-- ==================== 项目8: 外业调查项目 (有偏差) ====================
UPDATE sys_project SET status='active' WHERE id=8;

INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES
(8,'前期准备','资料收集设备调试人员培训','2026-05-01','2026-05-15','2026-05-02','2026-05-14','completed',3,0),
(8,'正式调查','全面开展外业调查工作','2026-05-16','2026-06-30','2026-05-18',NULL,'in_progress',3,1);

INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,review_status,submit_user_id,submit_time) VALUES
(17,8,'完成资料收集设备调试和人员培训准备就绪',100,'','培训覆盖率100%','培训记录及设备清单','passed',3,'2026-05-14 09:00:00'),
(18,8,'完成东部片区调查约8万亩开始西部片区',45,'西部片区地形复杂调查进度偏慢','每样地数据当日录入当日复核','东部片区调查数据','pending',3,'2026-06-10 14:00:00');

INSERT INTO sys_deviation (project_id,stage_id,report_id,type,description,reason,impact,status,create_user_id) VALUES
(8,18,18,'auto','西部片区进度偏慢','地形复杂调查难度大','整体进度或将延迟3-5天','open',2);

-- ==================== 确保所有项目都有成员 ====================
UPDATE sys_project_member SET status='confirmed' WHERE status IS NULL;

INSERT IGNORE INTO sys_project_member (project_id,user_id,role_in_project,status)
SELECT p.id, 2, 'manager', 'confirmed' FROM sys_project p
WHERE NOT EXISTS (SELECT 1 FROM sys_project_member m WHERE m.project_id=p.id AND m.user_id=2);

INSERT IGNORE INTO sys_project_member (project_id,user_id,role_in_project,status)
SELECT p.id, 3, 'engineer', 'confirmed' FROM sys_project p
WHERE NOT EXISTS (SELECT 1 FROM sys_project_member m WHERE m.project_id=p.id AND m.user_id=3);
