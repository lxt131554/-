USE pm_system;

-- ============================================================
-- v0.13: Migrate contract data from sys_project.procurement_info
--        to sys_project_contract table
-- ============================================================

INSERT IGNORE INTO sys_project_contract (project_id, contract_no, bid_status, contract_signed, customer_name, sign_date, contract_output_value, signed_amount, invoiced_total, received_total, receivable, settled_amount, oa_sync_time)
SELECT
    p.id AS project_id,
    TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, '合同编号：', -1), '\n', 1)) AS contract_no,
    TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, 'OA投标状态：', -1), '\n', 1)) AS bid_status,
    TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, '合同是否签订：', -1), '\n', 1)) AS contract_signed,
    TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.contacts, '合同甲方：', -1), '\n', 1)) AS customer_name,
    CASE WHEN TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, '签订时间：', -1), '\n', 1)) != ''
         THEN STR_TO_DATE(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, '签订时间：', -1), '\n', 1)), '%Y-%m-%d') END AS sign_date,
    CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, '合同产值：', -1), '\n', 1)) AS DECIMAL(18,2)) AS contract_output_value,
    CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, '签订金额：', -1), '\n', 1)) AS DECIMAL(18,2)) AS signed_amount,
    CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, '已开票总额：', -1), '\n', 1)) AS DECIMAL(18,2)) AS invoiced_total,
    CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, '已收总额：', -1), '\n', 1)) AS DECIMAL(18,2)) AS received_total,
    CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, '应收账款：', -1), '\n', 1)) AS DECIMAL(18,2)) AS receivable,
    CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(p.procurement_info, '已结算金额：', -1), '\n', 1)) AS DECIMAL(18,2)) AS settled_amount,
    NOW() AS oa_sync_time
FROM sys_project p
WHERE p.procurement_info IS NOT NULL
  AND p.procurement_info LIKE '%合同编号%'
  AND p.procurement_info NOT LIKE '%OA导入项目%'
  AND NOT EXISTS (SELECT 1 FROM sys_project_contract c WHERE c.project_id = p.id);

SELECT 'Migration completed.' AS status;

-- ============================================================
-- Verification queries
-- ============================================================

-- 1. Total migrated records
SELECT COUNT(*) AS migrated_count FROM sys_project_contract;

-- 2. Check all contract numbers are unique
SELECT COUNT(*) AS duplicate_contract_nos
FROM (
    SELECT contract_no, COUNT(*) AS cnt
    FROM sys_project_contract
    GROUP BY contract_no
    HAVING cnt > 1
) AS dupes;

-- 3. Check for NULL or empty contract numbers
SELECT COUNT(*) AS null_or_empty_contract_nos
FROM sys_project_contract
WHERE contract_no IS NULL OR TRIM(contract_no) = '';

-- 4. Count of OA projects in sys_project that have contract-related procurement_info
SELECT COUNT(*) AS oa_projects_with_contract
FROM sys_project p
WHERE p.procurement_info IS NOT NULL
  AND p.procurement_info LIKE '%合同编号%';

-- 5. Count of OA import projects (description starts with OA导入项目)
SELECT COUNT(*) AS oa_import_projects
FROM sys_project p
WHERE p.description LIKE 'OA导入项目%';

-- 6. Match check: projects with both procurement_info contract data AND a contract record
SELECT COUNT(*) AS projects_with_contract_record
FROM sys_project p
INNER JOIN sys_project_contract c ON c.project_id = p.id;
