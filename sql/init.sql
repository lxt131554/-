-- 创建数据库
CREATE DATABASE IF NOT EXISTS pm_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pm_system;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录名',
    password VARCHAR(200) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL COMMENT 'admin/manager/engineer/leader',
    dept VARCHAR(100) COMMENT '部门',
    phone VARCHAR(20) COMMENT '电话',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- 项目表
CREATE TABLE IF NOT EXISTS sys_project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active/completed/closed',
    create_user_id BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '项目表';

-- 项目成员表
CREATE TABLE IF NOT EXISTS sys_project_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_in_project VARCHAR(20) NOT NULL COMMENT 'manager/engineer',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_project_user (project_id, user_id)
) COMMENT '项目成员表';

-- 项目阶段表
CREATE TABLE IF NOT EXISTS sys_project_stage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    stage_name VARCHAR(100) NOT NULL COMMENT '阶段名称',
    description TEXT COMMENT '阶段描述',
    plan_start DATE COMMENT '计划开始',
    plan_end DATE COMMENT '计划结束',
    actual_start DATE COMMENT '实际开始',
    actual_end DATE COMMENT '实际结束',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/in_progress/submitted/completed',
    assignee_id BIGINT COMMENT '责任人ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '项目阶段表';

-- 阶段填报表
CREATE TABLE IF NOT EXISTS sys_stage_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stage_id BIGINT NOT NULL COMMENT '阶段ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    content TEXT COMMENT '填报内容',
    progress_rate INT DEFAULT 0 COMMENT '进度百分比 0-100',
    problem TEXT COMMENT '当前问题',
    plan_start DATE COMMENT '计划开始',
    plan_end DATE COMMENT '计划结束',
    actual_start DATE COMMENT '实际开始',
    actual_end DATE COMMENT '实际结束',
    review_status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/passed/returned',
    review_comment TEXT COMMENT '审阅意见',
    submit_user_id BIGINT COMMENT '提交人ID',
    submit_time DATETIME COMMENT '提交时间',
    review_user_id BIGINT COMMENT '审阅人ID',
    review_time DATETIME COMMENT '审阅时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '阶段填报表';

-- 种子数据（密码均为 123456，已BCrypt加密）
INSERT INTO sys_user (username, password, real_name, role, dept) VALUES
('admin', '$2b$12$zxspDQ4qB5bHU.dAqQnnXe/ZR01PKYGGIxFa8o.lvBIzWE3muqMK.', '系统管理员', 'admin', '信息中心'),
('manager1', '$2b$12$zxspDQ4qB5bHU.dAqQnnXe/ZR01PKYGGIxFa8o.lvBIzWE3muqMK.', '张主任', 'manager', '规划一室'),
('engineer1', '$2b$12$zxspDQ4qB5bHU.dAqQnnXe/ZR01PKYGGIxFa8o.lvBIzWE3muqMK.', '李工', 'engineer', '规划一室'),
('leader1', '$2b$12$zxspDQ4qB5bHU.dAqQnnXe/ZR01PKYGGIxFa8o.lvBIzWE3muqMK.', '王院长', 'leader', '院领导');
