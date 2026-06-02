package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysProjectStage;
import com.pm.security.LoginUser;
import com.pm.service.SysProjectStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StageController {

    private final SysProjectStageService stageService;

    @GetMapping("/projects/{projectId}/stages")
    public Result<List<SysProjectStage>> listStages(@PathVariable Long projectId) {
        return Result.ok(stageService.listByProjectId(projectId));
    }

    @PostMapping("/projects/{projectId}/stages")
    public Result<SysProjectStage> addStage(@PathVariable Long projectId,
                                            @RequestBody SysProjectStage stage) {
        stage.setProjectId(projectId);
        stage.setStatus("pending");
        stageService.save(stage);
        return Result.ok(stage);
    }

    @PutMapping("/projects/{projectId}/stages/{stageId}")
    public Result<SysProjectStage> updateStage(@PathVariable Long projectId,
                                               @PathVariable Long stageId,
                                               @RequestBody SysProjectStage stage) {
        stage.setId(stageId);
        stage.setProjectId(projectId);
        stageService.updateById(stage);
        return Result.ok(stage);
    }

    @DeleteMapping("/projects/{projectId}/stages/{stageId}")
    public Result<?> deleteStage(@PathVariable Long projectId, @PathVariable Long stageId) {
        stageService.removeById(stageId);
        return Result.ok();
    }

    @GetMapping("/stages/my-tasks")
    public Result<List<SysProjectStage>> myTasks(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.ok(stageService.listMyTasks(loginUser.getUser().getId()));
    }

    @PostMapping("/stages/template/{projectId}")
    public Result<List<SysProjectStage>> applyTemplate(@PathVariable Long projectId,
                                                       @RequestBody List<SysProjectStage> templateStages) {
        List<SysProjectStage> created = new ArrayList<>();
        for (int i = 0; i < templateStages.size(); i++) {
            SysProjectStage stage = templateStages.get(i);
            stage.setProjectId(projectId);
            stage.setStatus("pending");
            stage.setSortOrder(i);
            stage.setActualStart(null);
            stage.setActualEnd(null);
            stage.setId(null);
            stageService.save(stage);
            created.add(stage);
        }
        return Result.ok(created);
    }
}
