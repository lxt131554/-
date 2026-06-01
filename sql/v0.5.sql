-- V0.5: 变更控制 + 成果评审与审批
USE pm_system;

CREATE TABLE IF NOT EXISTS sys_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    content TEXT COMMENT '变更核心内容',
    confirm_time DATE COMMENT '与业主确认变更时间',
    impact TEXT COMMENT '变更影响范围',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/confirmed',
    create_user_id BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '变更记录表';

CREATE TABLE IF NOT EXISTS sys_approval (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    review_situation TEXT COMMENT '上级主管部门/专家评审情况',
    fail_reason TEXT COMMENT '评审/审批未通过及原因',
    confirm_time DATE COMMENT '最终确认时间',
    create_user_id BIGINT COMMENT '填报人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '成果评审与审批表';
