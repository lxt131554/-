package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_experience")
public class SysExperience {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String reusableExperience;
    private String shortcomings;
    private String risks;
    private String improvement;
    private Long createUserId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String projectName;
}
