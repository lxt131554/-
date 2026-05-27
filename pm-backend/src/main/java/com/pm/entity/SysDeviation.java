package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_deviation")
public class SysDeviation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long stageId;
    private Long reportId;
    private String type;
    private String description;
    private String reason;
    private String impact;
    private String status;
    private Long createUserId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime closeTime;

    @TableField(exist = false)
    private String projectName;
    @TableField(exist = false)
    private String stageName;
    @TableField(exist = false)
    private String createUserName;
}
