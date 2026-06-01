package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_project")
public class SysProject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String status;
    private Long createUserId;

    // V0.7: 启动与策划字段
    private String customerLevel;
    private String contacts;
    private String achievementType;
    private String approvalRequirements;
    private String canUndertake;
    private String mainRisks;
    private String keyConstraints;
    private String deliverableRequirements;
    private String approvalPath;
    private String hrAllocation;
    private String expectedOutputs;
    private String coreMaterials;
    private String teamSetup;
    private String coreStrategy;
    private String bidSituation;
    private String procurementInfo;
    private String acquisitionResult;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
