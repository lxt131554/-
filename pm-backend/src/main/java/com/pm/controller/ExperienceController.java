package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysExperience;
import com.pm.entity.SysProject;
import com.pm.mapper.SysProjectMapper;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<List<SysExperience>> listAll(@AuthenticationPrincipal LoginUser loginUser) {
        List<SysExperience> list = experienceService.listAll();
        if (!accessService.isAdmin(loginUser.getUser()) && !accessService.isLeader(loginUser.getUser())) {
            list.removeIf(exp -> !accessService.canViewProject(exp.getProjectId(), loginUser.getUser()));
        }
        for (SysExperience exp : list) {
            SysProject project = projectMapper.selectById(exp.getProjectId());
            if (project != null) exp.setProjectName(project.getName());
        }
        return Result.ok(list);
    }
}
