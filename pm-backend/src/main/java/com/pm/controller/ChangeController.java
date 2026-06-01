package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysChange;
import com.pm.security.LoginUser;
import com.pm.service.SysChangeService;
import com.pm.service.SysProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChangeController {

    private final SysChangeService changeService;
    private final SysProjectService projectService;

    @GetMapping("/api/projects/{projectId}/changes")
    public Result<List<SysChange>> listByProject(@PathVariable Long projectId) {
        List<SysChange> list = changeService.listByProject(projectId);
        return Result.ok(list);
    }

    @PostMapping("/api/projects/{projectId}/changes")
    public Result<SysChange> create(@PathVariable Long projectId,
                                    @RequestBody SysChange change,
                                    @AuthenticationPrincipal LoginUser loginUser) {
        change.setProjectId(projectId);
        change.setStatus("pending");
        change.setCreateUserId(loginUser.getUser().getId());
        changeService.save(change);
        return Result.ok(change);
    }

    @GetMapping("/api/changes/{id}")
    public Result<SysChange> getById(@PathVariable Long id) {
        SysChange c = changeService.getById(id);
        if (c == null) {
            return Result.fail("变更不存在");
        }
        com.pm.entity.SysProject p = projectService.getById(c.getProjectId());
        if (p != null) {
            c.setProjectName(p.getName());
        }
        return Result.ok(c);
    }

    @PutMapping("/api/changes/{id}/confirm")
    public Result<?> confirm(@PathVariable Long id) {
        changeService.confirm(id);
        return Result.ok();
    }
}
