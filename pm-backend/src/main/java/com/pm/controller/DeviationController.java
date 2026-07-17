package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.common.Result;
import com.pm.entity.SysDeviation;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectStage;
import com.pm.mapper.SysDeviationMapper;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysProjectStageMapper;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.CacheEvictionService;
import com.pm.service.SysDeviationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deviations")
@RequiredArgsConstructor
public class DeviationController {

    private final SysDeviationService deviationService;
    private final SysDeviationMapper deviationMapper;
    private final SysProjectMapper projectMapper;
    private final SysProjectStageMapper stageMapper;
    private final ProjectAccessService accessService;
    private final CacheEvictionService cacheEvictionService;

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
        Page<SysDeviation> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<SysDeviation> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(SysDeviation::getProjectId, projectId);
        } else if (!accessService.isAdmin(loginUser.getUser()) && !accessService.isLeader(loginUser.getUser())) {
            List<Long> projectIds = accessService.listConfirmedProjectIds(loginUser.getUser());
            if (projectIds.isEmpty()) {
                wrapper.eq(SysDeviation::getProjectId, -1L);
            } else {
                wrapper.in(SysDeviation::getProjectId, projectIds);
            }
        }
        wrapper.orderByDesc(SysDeviation::getCreateTime);
        pageObj = deviationMapper.selectPage(pageObj, wrapper);
        // Batch enrich projectName and stageName
        for (SysDeviation d : pageObj.getRecords()) {
            SysProject p = projectMapper.selectById(d.getProjectId());
            if (p != null) d.setProjectName(p.getName());
            SysProjectStage s = stageMapper.selectById(d.getStageId());
            if (s != null) d.setStageName(s.getStageName());
        }
        return Result.ok(pageObj);
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
        cacheEvictionService.evictDashboardCaches();
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
        cacheEvictionService.evictDashboardCaches();
        return Result.ok();
    }
}
