package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_support_item")
public class SysSupportItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull(message = "所属项目不能为空")
    private Long projectId;
    @NotBlank(message = "支持事项标题不能为空")
    private String title;
    private String content;
    private Long applicantId;
    private Long handlerId;
    private LocalDate expectTime;
    private String status;
    private String reply;

    // V0.9: 上级支持及解决情况
    private String resolveNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String projectName;
    @TableField(exist = false)
    private String applicantName;
    @TableField(exist = false)
    private String handlerName;
}
