package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysProject;
import com.pm.service.SysDeviationService;
import com.pm.service.SysProjectService;
import com.pm.service.SysSupportItemService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/api/leader-dashboard")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        List<SysProject> allProjects = projectService.list();
        long activeProjects = allProjects.stream().filter(p -> "active".equals(p.getStatus())).count();
        long completedProjects = allProjects.stream().filter(p -> "completed".equals(p.getStatus())).count();
        long openDeviations = deviationService.listByProject(null).stream()
                .filter(d -> "open".equals(d.getStatus())).count();
        long pendingSupports = supportItemService.listAll("pending").size();

        data.put("activeProjects", activeProjects);
        data.put("completedProjects", completedProjects);
        data.put("openDeviations", openDeviations);
        data.put("pendingSupports", pendingSupports);
        data.put("projects", allProjects);

        return Result.ok(data);
    }
}
