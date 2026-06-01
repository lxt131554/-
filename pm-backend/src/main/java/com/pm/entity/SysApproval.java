package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_approval")
public class SysApproval {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String reviewSituation;
    private String failReason;
    private LocalDate confirmTime;
    private Long createUserId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String projectName;
}
