package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.common.Result;
import com.pm.entity.SysDeviation;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.mapper.SysDeviationMapper;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysStageReportMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysProjectStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StageController {

    private final SysProjectStageService stageService;
    private final SysStageReportMapper reportMapper;
    private final SysProjectMapper projectMapper;
    private final SysUserMapper userMapper;
    private final SysDeviationMapper deviationMapper;
    private final ProjectAccessService accessService;

    @GetMapping("/projects/{projectId}/stages")
    public Result<List<SysProjectStage>> listStages(@PathVariable Long projectId,
                                                    @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectView(projectId, loginUser.getUser());
        return Result.ok(stageService.listByProjectId(projectId));
    }

    @PostMapping("/projects/{projectId}/stages")
    public Result<SysProjectStage> addStage(@PathVariable Long projectId,
                                            @RequestBody SysProjectStage stage,
                                            @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(projectId, loginUser.getUser());
        stage.setProjectId(projectId);
        stage.setStatus("pending");
        stageService.save(stage);
        return Result.ok(stage);
    }

    @PutMapping("/projects/{projectId}/stages/{stageId}")
    public Result<SysProjectStage> updateStage(@PathVariable Long projectId,
                                               @PathVariable Long stageId,
                                               @RequestBody SysProjectStage stage,
                                               @AuthenticationPrincipal LoginUser loginUser) {
        SysProjectStage existing = stageService.getById(stageId);
        if (existing == null || !projectId.equals(existing.getProjectId())) {
            return Result.fail("阶段不存在");
        }
        accessService.requireStageManager(stageId, loginUser.getUser());
        stage.setId(stageId);
        stage.setProjectId(projectId);
        stageService.updateById(stage);
        return Result.ok(stage);
    }

    @DeleteMapping("/projects/{projectId}/stages/{stageId}")
    public Result<?> deleteStage(@PathVariable Long projectId, @PathVariable Long stageId,
                                 @AuthenticationPrincipal LoginUser loginUser) {
        SysProjectStage existing = stageService.getById(stageId);
        if (existing == null || !projectId.equals(existing.getProjectId())) {
            return Result.fail("阶段不存在");
        }
        accessService.requireStageManager(stageId, loginUser.getUser());
        stageService.removeById(stageId);
        return Result.ok();
    }

    @GetMapping("/stages/my-tasks")
    public Result<List<SysProjectStage>> myTasks(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.ok(stageService.listMyTasks(loginUser.getUser().getId()));
    }

    @PostMapping("/stages/template/{projectId}")
    public Result<List<SysProjectStage>> applyTemplate(@PathVariable Long projectId,
                                                       @RequestBody List<SysProjectStage> templateStages,
                                                       @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(projectId, loginUser.getUser());
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

    @GetMapping("/stages/{id}/detail")
    public Result<Map<String, Object>> stageDetail(@PathVariable Long id,
                                                   @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireStageView(id, loginUser.getUser());
        SysProjectStage stage = stageService.getById(id);
        if (stage == null) return Result.fail("阶段不存在");

        // Populate project name
        var project = projectMapper.selectById(stage.getProjectId());
        if (project != null) stage.setProjectName(project.getName());

        // Get all reports for this stage
        List<SysStageReport> reports = reportMapper.selectList(
            new LambdaQueryWrapper<SysStageReport>()
                .eq(SysStageReport::getStageId, id)
                .orderByDesc(SysStageReport::getCreateTime)
        );
        // Populate submit user names
        for (var r : reports) {
            if (r.getSubmitUserId() != null) {
                var user = userMapper.selectById(r.getSubmitUserId());
                if (user != null) r.setSubmitUserName(user.getRealName());
            }
            r.setAttachmentData(null); // don't send binary in list
        }

        // Get deviations for this stage
        List<SysDeviation> deviations = deviationMapper.selectList(
            new LambdaQueryWrapper<SysDeviation>()
                .eq(SysDeviation::getStageId, id)
                .orderByDesc(SysDeviation::getCreateTime)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("stage", stage);
        result.put("reports", reports);
        result.put("deviations", deviations);
        result.put("projectId", stage.getProjectId());
        result.put("projectName", project != null ? project.getName() : "");

        return Result.ok(result);
    }
}
