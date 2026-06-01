package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysProject;
import com.pm.service.SysDeviationService;
import com.pm.service.SysProjectService;
import com.pm.service.SysSupportItemService;
import com.pm.service.SysChangeService;
import com.pm.service.SysStageReportService;
import com.pm.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LeaderDashboardController {

    private final SysProjectService projectService;
    private final SysDeviationService deviationService;
    private final SysSupportItemService supportItemService;
    private final SysChangeService changeService;
    private final SysStageReportService reportService;

    @GetMapping("/api/leader-dashboard")
    public Result<Map<String, Object>> stats(@AuthenticationPrincipal LoginUser loginUser) {
        Map<String, Object> data = new HashMap<>();
        List<SysProject> allProjects = projectService.list();
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
        
        var pendingReports = reportService.listPendingReview(loginUser.getUser().getId());
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

        return Result.ok(data);
    }
}
