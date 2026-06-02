package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.*;
import com.pm.mapper.*;
import com.pm.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatisticsController {

    private final SysProjectService projectService;
    private final SysDeviationService deviationService;
    private final SysSupportItemService supportService;
    private final SysUserMapper userMapper;
    private final SysProjectStageMapper stageMapper;
    private final SysExperienceMapper experienceMapper;

    @GetMapping("/statistics")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();

        // 1. Project counts
        List<SysProject> allProjects = projectService.list();
        long totalProjects = allProjects.size();
        long activeProjects = allProjects.stream().filter(p -> "active".equals(p.getStatus())).count();
        long completedProjects = allProjects.stream().filter(p -> "completed".equals(p.getStatus())).count();

        data.put("totalProjects", totalProjects);
        data.put("activeProjects", activeProjects);
        data.put("completedProjects", completedProjects);

        // 2. Overdue stages
        List<SysProjectStage> allStages = stageMapper.selectList(null);
        long overdueStages = allStages.stream()
            .filter(s -> s.getPlanEnd() != null && s.getPlanEnd().isBefore(java.time.LocalDate.now())
                && !"completed".equals(s.getStatus()))
            .count();
        data.put("overdueStages", overdueStages);

        // 3. Open deviations
        long openDeviations = deviationService.listByProject(null).stream()
            .filter(d -> "open".equals(d.getStatus())).count();
        data.put("openDeviations", openDeviations);

        // 4. Pending supports
        long pendingSupports = supportService.listAll("pending").size();
        data.put("pendingSupports", pendingSupports);

        // 5. By department
        List<SysUser> allUsers = userMapper.selectList(null);
        Map<Long, String> userDeptMap = allUsers.stream()
            .collect(Collectors.toMap(SysUser::getId, u -> u.getDept() != null ? u.getDept() : "未知"));

        Map<String, Long> projectsByDept = allProjects.stream()
            .collect(Collectors.groupingBy(
                p -> userDeptMap.getOrDefault(p.getCreateUserId(), "未知"),
                Collectors.counting()
            ));

        // experiences by user dept
        List<SysExperience> allExperiences = experienceMapper.selectList(null);
        Map<String, Long> experiencesByDept = allExperiences.stream()
            .collect(Collectors.groupingBy(
                e -> userDeptMap.getOrDefault(e.getCreateUserId(), "未知"),
                Collectors.counting()
            ));

        List<Map<String, Object>> deptStats = new ArrayList<>();
        Set<String> allDepts = new HashSet<>(projectsByDept.keySet());
        allDepts.addAll(experiencesByDept.keySet());
        for (String dept : allDepts) {
            Map<String, Object> ds = new HashMap<>();
            ds.put("dept", dept);
            ds.put("projects", projectsByDept.getOrDefault(dept, 0L));
            ds.put("experiences", experiencesByDept.getOrDefault(dept, 0L));
            deptStats.add(ds);
        }
        data.put("deptStats", deptStats);

        // 6. Monthly project creation
        Map<String, Long> monthlyProjects = allProjects.stream()
            .filter(p -> p.getCreateTime() != null)
            .collect(Collectors.groupingBy(
                p -> p.getCreateTime().toLocalDate().withDayOfMonth(1).toString(),
                TreeMap::new,
                Collectors.counting()
            ));
        List<Map<String, Object>> monthlyStats = monthlyProjects.entrySet().stream()
            .map(e -> {
                Map<String, Object> m = new HashMap<>();
                m.put("month", e.getKey());
                m.put("count", e.getValue());
                return m;
            }).collect(Collectors.toList());
        data.put("monthlyStats", monthlyStats);

        return Result.ok(data);
    }
}
