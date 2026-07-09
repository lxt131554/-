package com.pm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.common.Result;
import com.pm.entity.SysUser;
import com.pm.mapper.SysUserMapper;
import com.pm.security.LoginUser;
import com.pm.service.CacheEvictionService;
import com.pm.service.ProjectAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProjectAccessService accessService;
    private final CacheEvictionService cacheEvictionService;

    @GetMapping
    public Result<Page<SysUser>> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @AuthenticationPrincipal LoginUser loginUser) {
        if (size > 100) size = 100;
        accessService.requireAdmin(loginUser.getUser());
        Page<SysUser> p = new Page<>(page, size);
        Page<SysUser> result = userMapper.selectPage(p, null);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(result);
    }

    @GetMapping("/available")
    public Result<List<Map<String, Object>>> available(@AuthenticationPrincipal LoginUser loginUser) {
        List<SysUser> users = userMapper.selectList(null);
        // Safety cap: limit to 200 users for member selection dropdowns
        if (users.size() > 200) {
            users = users.subList(0, 200);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysUser u : users) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", u.getId());
            info.put("realName", u.getRealName());
            info.put("username", u.getUsername());
            info.put("dept", u.getDept());
            info.put("role", u.getRole());
            list.add(info);
        }
        return Result.ok(list);
    }

    @PostMapping
    public Result<SysUser> create(@Valid @RequestBody SysUser user,
                                  @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireAdmin(loginUser.getUser());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        userMapper.insert(user);
        user.setPassword(null);
        cacheEvictionService.evictDashboardCaches();
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
        cacheEvictionService.evictDashboardCaches();
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
        cacheEvictionService.evictDashboardCaches();
        return Result.ok();
    }
}
