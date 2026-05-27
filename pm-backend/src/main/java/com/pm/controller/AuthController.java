package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysUser;
import com.pm.security.LoginUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/current-user")
    public Result<Map<String, Object>> currentUser(@AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null) {
            return Result.fail(401, "未登录");
        }
        SysUser user = loginUser.getUser();
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("realName", user.getRealName());
        info.put("role", user.getRole());
        info.put("dept", user.getDept());
        return Result.ok(info);
    }
}
