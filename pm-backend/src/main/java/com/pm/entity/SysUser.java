package com.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名不能超过50个字符")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度6-100个字符")
    private String password;
    @NotBlank(message = "姓名不能为空")
    private String realName;
    @NotBlank(message = "角色不能为空")
    private String role;
    private String dept;
    private String phone;
    private Boolean enabled;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
