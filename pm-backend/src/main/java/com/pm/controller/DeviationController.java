package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysDeviation;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysDeviationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deviations")
@RequiredArgsConstructor
public class DeviationController {

    private final SysDeviationService deviationService;
    private final ProjectAccessService accessService;

    @GetMapping
    public Result<List<SysDeviation>> list(
            @RequestParam(required = false) Long projectId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (projectId != null) {
            accessService.requireProjectView(projectId, loginUser.getUser());
        }
        List<SysDeviation> list = deviationService.listByProject(projectId);
        if (projectId == null && !accessService.isAdmin(loginUser.getUser()) && !accessService.isLeader(loginUser.getUser())) {
            list.removeIf(d -> !accessService.canViewProject(d.getProjectId(), loginUser.getUser()));
        }
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result<SysDeviation> getById(@PathVariable Long id,
                                        @AuthenticationPrincipal LoginUser loginUser) {
        SysDeviation d = deviationService.getById(id);
        if (d == null) {
            return Result.fail("偏差不存在");
        }
        accessService.requireProjectView(d.getProjectId(), loginUser.getUser());
        return Result.ok(d);
    }

    @PostMapping
    public Result<SysDeviation> create(@RequestBody SysDeviation deviation,
                                       @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectView(deviation.getProjectId(), loginUser.getUser());
        deviation.setType("manual");
        deviation.setStatus("open");
        deviation.setCreateUserId(loginUser.getUser().getId());
        deviationService.save(deviation);
        return Result.ok(deviation);
    }

    @PutMapping("/{id}/close")
    public Result<?> close(@PathVariable Long id,
                           @AuthenticationPrincipal LoginUser loginUser) {
        SysDeviation deviation = deviationService.getById(id);
        if (deviation == null) {
            return Result.fail("偏差不存在");
        }
        accessService.requireProjectManager(deviation.getProjectId(), loginUser.getUser());
        deviationService.closeDeviation(id);
        return Result.ok();
    }
}
