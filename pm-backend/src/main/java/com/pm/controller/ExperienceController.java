package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysExperience;
import com.pm.security.LoginUser;
import com.pm.service.SysExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExperienceController {

    private final SysExperienceService experienceService;

    @GetMapping("/api/projects/{projectId}/experience")
    public Result<SysExperience> getByProject(@PathVariable Long projectId) {
        SysExperience exp = experienceService.getByProjectId(projectId);
        return Result.ok(exp);
    }

    @PostMapping("/api/projects/{projectId}/experience")
    public Result<SysExperience> save(@PathVariable Long projectId,
                                      @RequestBody SysExperience experience,
                                      @AuthenticationPrincipal LoginUser loginUser) {
        experience.setProjectId(projectId);
        SysExperience saved = experienceService.saveOrUpdateExperience(experience, loginUser.getUser().getId());
        return Result.ok(saved);
    }

    @GetMapping("/api/experiences")
    public Result<List<SysExperience>> listAll() {
        List<SysExperience> list = experienceService.listAll();
        return Result.ok(list);
    }
}
