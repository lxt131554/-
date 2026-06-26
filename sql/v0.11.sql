USE pm_system;

-- 第一轮可试运行建议索引：用于提升项目列表、待办、偏差、支持事项等常用页面查询速度。
-- 注意：同一索引重复执行会报错，交付部署时请由实施人员确认后执行一次。
CREATE INDEX idx_project_status ON sys_project(status);
CREATE INDEX idx_project_create_user ON sys_project(create_user_id);
CREATE INDEX idx_stage_project_status ON sys_project_stage(project_id, status);
CREATE INDEX idx_stage_assignee_status ON sys_project_stage(assignee_id, status);
CREATE INDEX idx_report_stage_status ON sys_stage_report(stage_id, review_status);
CREATE INDEX idx_report_project_status ON sys_stage_report(project_id, review_status);
CREATE INDEX idx_report_submit_user ON sys_stage_report(submit_user_id);
CREATE INDEX idx_deviation_project_status ON sys_deviation(project_id, status);
CREATE INDEX idx_support_project_status ON sys_support_item(project_id, status);
CREATE INDEX idx_support_handler_status ON sys_support_item(handler_id, status);
CREATE INDEX idx_change_project_status ON sys_change(project_id, status);

-- 若正式交付前已清理重复数据，可再启用以下唯一约束，避免同一项目重复复盘/重复经验总结。
-- ALTER TABLE sys_review ADD UNIQUE KEY uk_review_project (project_id);
-- ALTER TABLE sys_experience ADD UNIQUE KEY uk_experience_project (project_id);
