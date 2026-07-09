package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.common.Result;
import com.pm.entity.SysExperience;
import com.pm.mapper.SysProjectMapper;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ExperienceController {

    private final SysExperienceService experienceService;
    private final ProjectAccessService accessService;
    private final SysProjectMapper projectMapper;

    @GetMapping("/api/projects/{projectId}/experience")
    public Result<SysExperience> getByProject(@PathVariable Long projectId,
                                              @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectView(projectId, loginUser.getUser());
        SysExperience exp = experienceService.getByProjectId(projectId);
        return Result.ok(exp);
    }

    @PostMapping("/api/projects/{projectId}/experience")
    public Result<SysExperience> save(@PathVariable Long projectId,
                                      @RequestBody SysExperience experience,
                                      @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(projectId, loginUser.getUser());
        experience.setProjectId(projectId);
        SysExperience saved = experienceService.saveOrUpdateExperience(experience, loginUser.getUser().getId());
        return Result.ok(saved);
    }

    @GetMapping("/api/experiences")
    public Result<Page<SysExperience>> listAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (size > 100) size = 100;
        Page<SysExperience> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<SysExperience> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysExperience::getCreateTime);
        experienceService.page(pageObj, wrapper);
        List<SysExperience> records = pageObj.getRecords();
        if (!accessService.isAdmin(loginUser.getUser()) && !accessService.isLeader(loginUser.getUser())) {
            records.removeIf(exp -> !accessService.canViewProject(exp.getProjectId(), loginUser.getUser()));
        }
        // Batch project name lookup
        Set<Long> projectIds = records.stream()
                .map(SysExperience::getProjectId).filter(id -> id != null).collect(Collectors.toSet());
        Map<Long, String> nameMap = new HashMap<>();
        if (!projectIds.isEmpty()) {
            projectMapper.selectBatchIds(projectIds)
                    .forEach(p -> nameMap.put(p.getId(), p.getName()));
        }
        for (SysExperience exp : records) {
            exp.setProjectName(nameMap.getOrDefault(exp.getProjectId(), ""));
        }
        return Result.ok(pageObj);
    }
}
