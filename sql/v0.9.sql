USE pm_system;
ALTER TABLE sys_project ADD COLUMN project_importance TEXT COMMENT '项目对双方重要性';
ALTER TABLE sys_project ADD COLUMN achievement_direction TEXT COMMENT '成果方向及附件';
ALTER TABLE sys_stage_report ADD COLUMN dept_review_note TEXT COMMENT '成果部门审核及核心问题';
ALTER TABLE sys_support_item ADD COLUMN resolve_note TEXT COMMENT '上级支持及解决情况';
