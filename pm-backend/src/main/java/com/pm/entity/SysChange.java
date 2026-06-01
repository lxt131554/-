package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_change")
public class SysChange {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String content;
    private LocalDate confirmTime;
    private String impact;
    private String status;
    private Long createUserId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String projectName;
}
