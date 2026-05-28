package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_review")
public class SysReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String overallDeviation;
    private String efficiencyRating;
    private String qualityRating;
    private String communicationNote;
    private Long createUserId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String projectName;
}
