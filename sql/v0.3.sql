-- V0.3: 偏差记录表 + 支持事项表
USE pm_system;

CREATE TABLE IF NOT EXISTS sys_deviation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    stage_id BIGINT COMMENT '关联阶段ID(可空)',
    report_id BIGINT COMMENT '关联填报ID(可空)',
    type VARCHAR(20) NOT NULL DEFAULT 'auto' COMMENT 'auto自动生成/manual手动创建',
    description TEXT COMMENT '偏差描述',
    reason TEXT COMMENT '偏差原因',
    impact TEXT COMMENT '影响范围',
    status VARCHAR(20) DEFAULT 'open' COMMENT 'open未关闭/closed已关闭',
    create_user_id BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    close_time DATETIME COMMENT '关闭时间'
) COMMENT '偏差记录表';

CREATE TABLE IF NOT EXISTS sys_support_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    title VARCHAR(200) NOT NULL COMMENT '事项标题',
    content TEXT COMMENT '详细描述',
    applicant_id BIGINT COMMENT '申请人ID',
    handler_id BIGINT COMMENT '处理人ID',
    expect_time DATE COMMENT '期望解决时间',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending待处理/resolved已解决',
    reply TEXT COMMENT '处理回复',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '支持事项表';
