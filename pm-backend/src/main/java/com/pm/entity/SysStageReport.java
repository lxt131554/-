package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_stage_report")
public class SysStageReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long stageId;
    private Long projectId;
    private String content;
    private Integer progressRate;
    private String problem;

    // V0.7: 阶段填报补全
    private String qualityControl;
    private String resultSummary;
    private String coordinationNote;

    private LocalDate planStart;
    private LocalDate planEnd;
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private String reviewStatus;
    private String reviewComment;
    private Long submitUserId;
    private LocalDateTime submitTime;
    private Long reviewUserId;
    private LocalDateTime reviewTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String attachmentName;
    private byte[] attachmentData;

    @TableField(exist = false)
    private String submitUserName;
    @TableField(exist = false)
    private String stageName;
    @TableField(exist = false)
    private String projectName;
}
