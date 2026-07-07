package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.common.Result;
import com.pm.entity.SysApproval;
import com.pm.entity.SysExperience;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysReview;
import com.pm.mapper.SysApprovalMapper;
import com.pm.mapper.SysExperienceMapper;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysReviewMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.service.SysDeviationService;
import com.pm.service.SysProjectService;
import com.pm.service.SysSupportItemService;
import com.pm.service.SysChangeService;
import com.pm.service.SysStageReportService;
import com.pm.service.SysProjectStageService;
import com.pm.service.ProjectAccessService;
import com.pm.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class LeaderDashboardController {

    private final SysProjectService projectService;
    private final SysDeviationService deviationService;
    private final SysSupportItemService supportItemService;
    private final SysChangeService changeService;
    private final SysStageReportService reportService;
    private final SysProjectStageService stageService;
    private final SysProjectMemberMapper memberMapper;
    private final SysUserMapper userMapper;
    private final SysReviewMapper reviewMapper;
    private final SysExperienceMapper experienceMapper;
    private final SysApprovalMapper approvalMapper;
    private final ProjectAccessService accessService;

    @GetMapping("/api/leader-dashboard")
    public Result<Map<String, Object>> stats(@AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireLeaderOrAdmin(loginUser.getUser());
        Map<String, Object> data = new HashMap<>();
        List<SysProject> allProjects = projectService.list();
        fillProjectOverview(allProjects);
        long activeProjects = allProjects.stream().filter(p -> "active".equals(p.getStatus())).count();
        long completedProjects = allProjects.stream().filter(p -> "completed".equals(p.getStatus())).count();
        var openDeviations = deviationService.listByProject(null).stream()
                .filter(d -> "open".equals(d.getStatus())).collect(java.util.stream.Collectors.toList());
        var pendingSupports = supportItemService.listAll("pending");

        var pendingChanges = changeService.list().stream()
                .filter(c -> "pending".equals(c.getStatus())).collect(java.util.stream.Collectors.toList());
        pendingChanges.forEach(c -> {
            SysProject p = projectService.getById(c.getProjectId());
            if (p != null) c.setProjectName(p.getName());
        });
        
        var pendingReports = reportService.listPendingReview(loginUser.getUser().getId(), loginUser.getUser().getRole());
        pendingReports.forEach(r -> r.setAttachmentData(null));

        data.put("activeProjects", activeProjects);
        data.put("completedProjects", completedProjects);
        data.put("openDeviations", openDeviations.size());
        data.put("openDeviationList", openDeviations);
        data.put("pendingSupports", pendingSupports.size());
        data.put("pendingSupportList", pendingSupports);
        
        data.put("pendingChanges", pendingChanges);
        data.put("pendingReports", pendingReports);
        
        data.put("projects", allProjects);

        // Closeout stats for completed projects
        long closeoutComplete = 0;
        long closeoutIncomplete = 0;
        List<Map<String, Object>> closeoutList = new ArrayList<>();

        for (SysProject p : allProjects) {
            if (!"completed".equals(p.getStatus())) continue;

            long revCount = reviewMapper.selectCount(new LambdaQueryWrapper<SysReview>().eq(SysReview::getProjectId, p.getId()));
            long expCount = experienceMapper.selectCount(new LambdaQueryWrapper<SysExperience>().eq(SysExperience::getProjectId, p.getId()));
            long appCount = approvalMapper.selectCount(new LambdaQueryWrapper<SysApproval>().eq(SysApproval::getProjectId, p.getId()));
            long done = (revCount > 0 ? 1 : 0) + (expCount > 0 ? 1 : 0) + (appCount > 0 ? 1 : 0);

            if (done == 3) closeoutComplete++;
            else closeoutIncomplete++;

            if (done < 3) {
                List<String> missing = new ArrayList<>();
                if (revCount == 0) missing.add("项目自评");
                if (expCount == 0) missing.add("经验总结");
                if (appCount == 0) missing.add("成果评审记录");
                closeoutList.add(Map.of("projectId", p.getId(), "projectName", p.getName(), "done", done, "total", 3, "missing", String.join("、", missing)));
            }
        }

        data.put("closeoutComplete", closeoutComplete);
        data.put("closeoutIncomplete", closeoutIncomplete);
        data.put("closeoutList", closeoutList);

        return Result.ok(data);
    }

    private void fillProjectOverview(List<SysProject> projects) {
        if (projects == null || projects.isEmpty()) {
            return;
        }

        List<Long> projectIds = projects.stream().map(SysProject::getId).collect(Collectors.toList());
        Map<Long, String> managerNameMap = buildManagerNameMap(projectIds);
        Map<Long, String> currentStageNameMap = buildCurrentStageNameMap(projectIds);

        for (SysProject project : projects) {
            project.setManagerName(managerNameMap.getOrDefault(project.getId(), ""));
            project.setCurrentStageName(currentStageNameMap.getOrDefault(project.getId(), ""));
        }
    }

    private Map<Long, String> buildManagerNameMap(List<Long> projectIds) {
        Map<Long, String> result = new LinkedHashMap<>();
        if (projectIds.isEmpty()) {
            return result;
        }

        List<SysProjectMember> managerMembers = memberMapper.selectList(new LambdaQueryWrapper<SysProjectMember>()
                .in(SysProjectMember::getProjectId, projectIds)
                .eq(SysProjectMember::getRoleInProject, "manager")
                .eq(SysProjectMember::getStatus, "confirmed"));
        Set<Long> userIds = managerMembers.stream()
                .map(SysProjectMember::getUserId)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds)
                    .forEach(user -> userNameMap.put(user.getId(), user.getRealName()));
        }
        for (SysProjectMember member : managerMembers) {
            result.putIfAbsent(member.getProjectId(), userNameMap.getOrDefault(member.getUserId(), ""));
        }
        return result;
    }

    private Map<Long, String> buildCurrentStageNameMap(List<Long> projectIds) {
        Map<Long, String> result = new HashMap<>();
        if (projectIds.isEmpty()) {
            return result;
        }

        List<SysProjectStage> stages = stageService.list(new LambdaQueryWrapper<SysProjectStage>()
                .in(SysProjectStage::getProjectId, projectIds)
                .orderByAsc(SysProjectStage::getProjectId)
                .orderByAsc(SysProjectStage::getSortOrder));
        Map<Long, List<SysProjectStage>> stageMap = new LinkedHashMap<>();
        for (SysProjectStage stage : stages) {
            stageMap.computeIfAbsent(stage.getProjectId(), key -> new ArrayList<>()).add(stage);
        }

        for (Map.Entry<Long, List<SysProjectStage>> entry : stageMap.entrySet()) {
            List<SysProjectStage> projectStages = entry.getValue();
            SysProjectStage currentStage = projectStages.stream()
                    .filter(stage -> !"completed".equals(stage.getStatus()))
                    .findFirst()
                    .orElse(projectStages.get(projectStages.size() - 1));
            result.put(entry.getKey(), currentStage != null ? currentStage.getStageName() : "");
        }
        return result;
    }
}
