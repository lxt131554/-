-- V0.7: 启动与策划字段 + 阶段填报补全
USE pm_system;

-- 启动与策划: sys_project 新增17字段
ALTER TABLE sys_project ADD COLUMN customer_level VARCHAR(50) COMMENT '客户等级/项目分级';
ALTER TABLE sys_project ADD COLUMN contacts TEXT COMMENT '双方点对点联系人';
ALTER TABLE sys_project ADD COLUMN achievement_type VARCHAR(200) COMMENT '成果产出类型';
ALTER TABLE sys_project ADD COLUMN approval_requirements TEXT COMMENT '审核审批要求';
ALTER TABLE sys_project ADD COLUMN can_undertake TEXT COMMENT '能否承接项目基本判断';
ALTER TABLE sys_project ADD COLUMN main_risks TEXT COMMENT '项目主要风险';
ALTER TABLE sys_project ADD COLUMN key_constraints TEXT COMMENT '项目关键约束';
ALTER TABLE sys_project ADD COLUMN deliverable_requirements TEXT COMMENT '成果交付要求';
ALTER TABLE sys_project ADD COLUMN approval_path TEXT COMMENT '预计审批路径';
ALTER TABLE sys_project ADD COLUMN hr_allocation TEXT COMMENT '人力资源配置';
ALTER TABLE sys_project ADD COLUMN expected_outputs TEXT COMMENT '各阶段产出预计成果';
ALTER TABLE sys_project ADD COLUMN core_materials TEXT COMMENT '前期需收集最核心资料';
ALTER TABLE sys_project ADD COLUMN team_setup TEXT COMMENT '项目组组建情况';
ALTER TABLE sys_project ADD COLUMN core_strategy TEXT COMMENT '确保完成核心要素研判和策略';
ALTER TABLE sys_project ADD COLUMN bid_situation TEXT COMMENT '具体投标情况';
ALTER TABLE sys_project ADD COLUMN procurement_info TEXT COMMENT '采购程序及负责人';
ALTER TABLE sys_project ADD COLUMN acquisition_result TEXT COMMENT '最终获取情况及失败原因';

-- 阶段填报补全: sys_stage_report 新增3字段
ALTER TABLE sys_stage_report ADD COLUMN quality_control TEXT COMMENT '质量管控措施';
ALTER TABLE sys_stage_report ADD COLUMN result_summary TEXT COMMENT '阶段成果说明';
ALTER TABLE sys_stage_report ADD COLUMN coordination_note TEXT COMMENT '沟通协调情况';
