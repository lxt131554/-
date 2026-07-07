USE pm_system;

CREATE TABLE IF NOT EXISTS sys_project_contract (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    contract_no VARCHAR(100) NOT NULL COMMENT '合同编号',
    bid_status VARCHAR(50) COMMENT '投标状态',
    contract_signed VARCHAR(50) COMMENT '合同是否签订',
    customer_name VARCHAR(200) COMMENT '合同甲方',
    sign_date DATE COMMENT '签订日期',
    category VARCHAR(100) COMMENT '项目类别',
    sub_category VARCHAR(100) COMMENT '项目子类别',
    contract_output_value DECIMAL(18,2) COMMENT '合同产值',
    signed_amount DECIMAL(18,2) COMMENT '签订金额',
    invoiced_total DECIMAL(18,2) COMMENT '已开票总额',
    received_total DECIMAL(18,2) COMMENT '已收总额',
    receivable DECIMAL(18,2) COMMENT '应收账款',
    settled_amount DECIMAL(18,2) COMMENT '已结算金额',
    oa_sync_time DATETIME COMMENT 'OA同步时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contract_no (contract_no),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目合同表';
