package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_project_contract")
public class SysProjectContract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String contractNo;
    private String bidStatus;
    private String contractSigned;
    private String customerName;
    private LocalDate signDate;
    private String category;
    private String subCategory;
    private BigDecimal contractOutputValue;
    private BigDecimal signedAmount;
    private BigDecimal invoicedTotal;
    private BigDecimal receivedTotal;
    private BigDecimal receivable;
    private BigDecimal settledAmount;
    private LocalDateTime oaSyncTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
