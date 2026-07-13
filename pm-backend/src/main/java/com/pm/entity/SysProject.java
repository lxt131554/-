package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_project")
public class SysProject {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 255, message = "项目名称不能超过255个字符")
    private String name;
    private String projectNo;
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

    // V0.9: 项目重要性及成果方向
    private String projectImportance;
    private String achievementDirection;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<SysDeviation> deviations;
    @TableField(exist = false)
    private List<SysSupportItem> supportItems;
    @TableField(exist = false)
    private List<SysSupportItem> supports;
    @TableField(exist = false)
    private String managerName;
    @TableField(exist = false)
    private String currentStageName;
}
