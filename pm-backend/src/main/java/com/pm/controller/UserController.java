package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysUser;
import com.pm.mapper.SysUserMapper;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProjectAccessService accessService;

    @GetMapping
    public Result<List<SysUser>> list(@AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireAdmin(loginUser.getUser());
        List<SysUser> users = userMapper.selectList(null);
        users.forEach(u -> u.setPassword(null)); // never expose password
        return Result.ok(users);
    }

    @PostMapping
    public Result<SysUser> create(@Valid @RequestBody SysUser user,
                                  @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireAdmin(loginUser.getUser());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        userMapper.insert(user);
        user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping("/{id}")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                  @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireAdmin(loginUser.getUser());
        SysUser user = userMapper.selectById(id);
        if (user == null) return Result.fail("用户不存在");
        if (body.get("realName") != null) user.setRealName((String) body.get("realName"));
        if (body.get("role") != null) user.setRole((String) body.get("role"));
        if (body.get("dept") != null) user.setDept((String) body.get("dept"));
        if (body.get("phone") != null) user.setPhone((String) body.get("phone"));
        if (body.get("password") != null && !((String) body.get("password")).isEmpty()) {
            user.setPassword(passwordEncoder.encode((String) body.get("password")));
        }
        userMapper.updateById(user);
        user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping("/{id}/toggle")
    public Result<?> toggle(@PathVariable Long id,
                            @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireAdmin(loginUser.getUser());
        if (loginUser.getUser().getId().equals(id)) {
            return Result.fail(400, "不能停用自己");
        }
        SysUser user = userMapper.selectById(id);
        if (user == null) return Result.fail("用户不存在");
        user.setEnabled(!(user.getEnabled() != null && user.getEnabled()));
        userMapper.updateById(user);
        return Result.ok();
    }
}
