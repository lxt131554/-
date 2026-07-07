package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysUser;
import com.pm.mapper.SysUserMapper;
import com.pm.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody Map<String, String> body,
                                    @AuthenticationPrincipal LoginUser loginUser) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return Result.fail(400, "请输入旧密码和新密码");
        }
        if (newPassword.length() < 6) {
            return Result.fail(400, "新密码至少6位");
        }

        SysUser user = userMapper.selectById(loginUser.getUser().getId());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.fail(400, "旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.ok();
    }
}
