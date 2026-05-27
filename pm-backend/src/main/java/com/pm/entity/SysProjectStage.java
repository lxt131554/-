package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_project_stage")
public class SysProjectStage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String stageName;
    private String description;
    private LocalDate planStart;
    private LocalDate planEnd;
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private String status;
    private Long assigneeId;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String assigneeName;
    @TableField(exist = false)
    private SysStageReport latestReport;
}
