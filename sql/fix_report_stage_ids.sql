-- 修复报告引用的失效 stage_id，映射到数据库实际阶段 ID
-- 种子脚本中的 stage_id(8,9,12,14,18) 与数据库实际阶段 ID 不一致
USE pm_system;

-- 报告ID → 旧stageID → 新stageID 映射（基于项目名+阶段名匹配）
-- report#20: projectId=3 攀枝花市林业产业发展规划 → stageId=44 规划编制
-- report#21: projectId=4 阿坝州草原生态修复实施方案 → stageId=46 外业调查
-- report#23: projectId=5 射洪市国土空间规划方案设计 → stageId=49 方案设计
-- report#25: projectId=6 洪雅县航拍测绘及数据解算 → stageId=51 内业处理
-- report#28: projectId=8 绵竹市外业调查与数据分析 → stageId=55 正式调查

UPDATE sys_stage_report SET stage_id=44 WHERE id=20;
UPDATE sys_stage_report SET stage_id=46 WHERE id=21;
UPDATE sys_stage_report SET stage_id=49 WHERE id=23;
UPDATE sys_stage_report SET stage_id=51 WHERE id=25;
UPDATE sys_stage_report SET stage_id=55 WHERE id=28;

-- 同时修复偏差表中的 stage_id
UPDATE sys_deviation SET stage_id=44 WHERE stage_id=8 AND project_id=3;
UPDATE sys_deviation SET stage_id=51 WHERE stage_id=14 AND project_id=6;
UPDATE sys_deviation SET stage_id=55 WHERE stage_id=18 AND project_id=8;

SELECT '报告 stage_id 修复完成' AS result;
