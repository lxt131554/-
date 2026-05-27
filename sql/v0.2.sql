-- V0.2: 成果提交附件字段
USE pm_system;

ALTER TABLE sys_stage_report
ADD COLUMN attachment_name VARCHAR(500) COMMENT '附件文件名',
ADD COLUMN attachment_data MEDIUMBLOB COMMENT '附件二进制数据';
