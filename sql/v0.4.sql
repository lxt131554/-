-- V0.4: 项目自评 + 经验总结
USE pm_system;

CREATE TABLE IF NOT EXISTS sys_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    overall_deviation TEXT COMMENT '项目整体执行偏差',
    efficiency_rating TEXT COMMENT '项目综合效率评价',
    quality_rating TEXT COMMENT '项目质量评价',
    communication_note TEXT COMMENT '全过程沟通情况',
    create_user_id BIGINT COMMENT '填报人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '项目自评表';

CREATE TABLE IF NOT EXISTS sys_experience (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    reusable_experience TEXT COMMENT '值得复用的经验',
    shortcomings TEXT COMMENT '相关短板或缺陷',
    risks TEXT COMMENT '风险点',
    improvement TEXT COMMENT '改进建议',
    create_user_id BIGINT COMMENT '填报人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '经验总结表';
