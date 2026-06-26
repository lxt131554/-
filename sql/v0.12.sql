USE pm_system;

-- Excel 台账第 33 列：收尾与复盘/复盘评价闸/自评/上级支持事项自评
ALTER TABLE sys_review
    ADD COLUMN support_evaluation TEXT COMMENT '上级支持事项自评' AFTER overall_deviation;
