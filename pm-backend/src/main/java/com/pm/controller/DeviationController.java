package com.pm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.common.Result;
import com.pm.entity.SysDeviation;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysDeviationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/deviations")
@RequiredArgsConstructor
public class DeviationController {

    private final SysDeviationService deviationService;
    private final ProjectAccessService accessService;

    @GetMapping
    public Result<Page<SysDeviation>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (size > 100) size = 100;
        if (projectId != null) {
            accessService.requireProjectView(projectId, loginUser.getUser());
        }
        List<SysDeviation> list = deviationService.listByProject(projectId);
        if (projectId == null && !accessService.isAdmin(loginUser.getUser()) && !accessService.isLeader(loginUser.getUser())) {
            list.removeIf(d -> !accessService.canViewProject(d.getProjectId(), loginUser.getUser()));
        }
        long total = list.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, (int) total);
        Page<SysDeviation> p = new Page<>(page, size, total);
        p.setRecords(start < total ? list.subList(start, end) : Collections.emptyList());
        return Result.ok(p);
    }

    @GetMapping("/{id}")
    public Result<SysDeviation> getById(@PathVariable Long id,
                                        @AuthenticationPrincipal LoginUser loginUser) {
        SysDeviation d = deviationService.getDetail(id);
        if (d == null) {
            return Result.fail("偏差不存在");
        }
        accessService.requireProjectView(d.getProjectId(), loginUser.getUser());
        return Result.ok(d);
    }

    @PostMapping
    public Result<SysDeviation> create(@Valid @RequestBody SysDeviation deviation,
                                       @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(deviation.getProjectId(), loginUser.getUser());
        accessService.requireProjectActive(deviation.getProjectId());
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
