package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_project_stage")
public class SysProjectStage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    @NotBlank(message = "阶段名称不能为空")
    private String stageName;
    private String description;
    private LocalDate planStart;
    private LocalDate planEnd;
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private String status;
    @NotNull(message = "责任人不能为空")
    private Long assigneeId;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String projectName;
    @TableField(exist = false)
    private String assigneeName;
    @TableField(exist = false)
    private SysStageReport latestReport;
}
