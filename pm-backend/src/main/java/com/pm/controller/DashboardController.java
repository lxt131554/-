package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.common.Result;
import com.pm.entity.SysDeviation;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.entity.SysSupportItem;
import com.pm.entity.SysChange;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.security.LoginUser;
import com.pm.service.SysDeviationService;
import com.pm.service.SysProjectStageService;
import com.pm.service.SysStageReportService;
import com.pm.service.SysSupportItemService;
import com.pm.service.SysChangeService;
import com.pm.service.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysProjectStageService stageService;
    private final SysStageReportService reportService;
    private final SysDeviationService deviationService;
    private final SysSupportItemService supportItemService;
    private final SysChangeService changeService;
    private final ProjectAccessService accessService;
    private final SysProjectMapper projectMapper;
    private final SysProjectMemberMapper memberMapper;
    private final SysUserMapper userMapper;

    @GetMapping
    @Cacheable(cacheNames = "dashboardCache", key = "'user:' + #loginUser.getUser().getId() + ':role:' + #loginUser.getUser().getRole()", unless = "#result == null || #result.data == null")
    public Result<Map<String, Object>> stats(@AuthenticationPrincipal LoginUser loginUser) {
        String role = loginUser.getUser().getRole();
        Long userId = loginUser.getUser().getId();
        LocalDate today = LocalDate.now();

        Map<String, Object> result = new LinkedHashMap<>();

        switch (role) {
            case "engineer": {
                long todo = stageService.count(new LambdaQueryWrapper<SysProjectStage>()
                        .eq(SysProjectStage::getAssigneeId, userId)
                        .ne(SysProjectStage::getStatus, "completed"));
                long returned = reportService.count(new LambdaQueryWrapper<SysStageReport>()
                        .eq(SysStageReport::getSubmitUserId, userId)
                        .eq(SysStageReport::getReviewStatus, "returned"));
                long overdue = stageService.count(new LambdaQueryWrapper<SysProjectStage>()
                        .eq(SysProjectStage::getAssigneeId, userId)
                        .ne(SysProjectStage::getStatus, "completed")
                        .isNotNull(SysProjectStage::getPlanEnd)
                        .lt(SysProjectStage::getPlanEnd, today));
                long nearDeadline = stageService.count(new LambdaQueryWrapper<SysProjectStage>()
                        .eq(SysProjectStage::getAssigneeId, userId)
                        .ne(SysProjectStage::getStatus, "completed")
                        .isNotNull(SysProjectStage::getPlanEnd)
                        .ge(SysProjectStage::getPlanEnd, today)
                        .le(SysProjectStage::getPlanEnd, today.plusDays(7)));
                result.put("todo", todo);
                result.put("returned", returned);
                result.put("overdue", overdue);
                result.put("nearDeadline", nearDeadline);

                // My confirmed projects
                List<Long> myProjectIds = memberMapper.selectList(new LambdaQueryWrapper<SysProjectMember>()
                        .eq(SysProjectMember::getUserId, userId)
                        .eq(SysProjectMember::getStatus, "confirmed"))
                        .stream().map(SysProjectMember::getProjectId).distinct().collect(Collectors.toList());
                result.put("myProjects", buildProjectCards(myProjectIds, userId));
                result.put("pendingStages", buildEngineerPendingStages(userId));
                break;
            }
            case "manager": {
                var pendingReports = reportService.listPendingReview(userId, role);
                long pendingReview = pendingReports.size();
                long pendingAchievement = pendingReports.stream()
                        .filter(r -> r.getAttachmentName() != null && !r.getAttachmentName().isEmpty())
                        .count();
                long openDeviations = deviationService.listByProject(null).stream()
                        .filter(d -> "open".equals(d.getStatus()))
                        .filter(d -> accessService.canViewProject(d.getProjectId(), loginUser.getUser()))
                        .count();
                long pendingSupports = supportItemService.listAll("pending").stream()
                        .filter(s -> accessService.canViewProject(s.getProjectId(), loginUser.getUser())
                                || userId.equals(s.getApplicantId()))
                        .count();
                long pendingChanges = changeService.list().stream()
                        .filter(c -> "pending".equals(c.getStatus()))
                        .filter(c -> accessService.canViewProject(c.getProjectId(), loginUser.getUser()))
                        .count();
                long reviewOverdue = pendingReports.stream()
                        .filter(r -> r.getSubmitTime() != null)
                        .filter(r -> r.getSubmitTime().isBefore(LocalDateTime.now().minusHours(48)))
                        .count();

                // My managed project count
                long myProjectCount = memberMapper.selectCount(new LambdaQueryWrapper<SysProjectMember>()
                        .eq(SysProjectMember::getUserId, userId)
                        .eq(SysProjectMember::getRoleInProject, "manager")
                        .eq(SysProjectMember::getStatus, "confirmed"));
                // Near-deadline stages across managed projects
                List<Long> managedProjectIds = memberMapper.selectList(new LambdaQueryWrapper<SysProjectMember>()
                        .eq(SysProjectMember::getUserId, userId)
                        .eq(SysProjectMember::getRoleInProject, "manager")
                        .eq(SysProjectMember::getStatus, "confirmed"))
                        .stream().map(SysProjectMember::getProjectId).distinct().collect(Collectors.toList());
                long nearDeadline = 0;
                if (!managedProjectIds.isEmpty()) {
                    nearDeadline = stageService.count(new LambdaQueryWrapper<SysProjectStage>()
                            .in(SysProjectStage::getProjectId, managedProjectIds)
                            .ne(SysProjectStage::getStatus, "completed")
                            .isNotNull(SysProjectStage::getPlanEnd)
                            .ge(SysProjectStage::getPlanEnd, today)
                            .le(SysProjectStage::getPlanEnd, today.plusDays(7)));
                }

                result.put("myProjectCount", myProjectCount);
                result.put("pendingReview", pendingReview);
                result.put("pendingAchievement", pendingAchievement);
                result.put("openDeviations", openDeviations);
                result.put("pendingSupports", pendingSupports);
                result.put("pendingChanges", pendingChanges);
                result.put("reviewOverdue", reviewOverdue);
                result.put("nearDeadline", nearDeadline);

                // Pending review items (top 5)
                result.put("pendingReviewItems", pendingReports.stream().limit(5).map(r -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("reportId", r.getId());
                    item.put("projectId", r.getProjectId());
                    item.put("projectName", r.getProjectName());
                    item.put("stageId", r.getStageId());
                    item.put("stageName", r.getStageName());
                    item.put("submitUserName", r.getSubmitUserName());
                    item.put("submitTime", r.getSubmitTime());
                    return item;
                }).collect(Collectors.toList()));

                // My projects
                result.put("myProjects", buildProjectCards(managedProjectIds, userId));
                break;
            }
            case "leader": {
                long openDeviations = deviationService.count(new LambdaQueryWrapper<SysDeviation>()
                        .eq(SysDeviation::getStatus, "open"));
                long pendingSupports = supportItemService.count(new LambdaQueryWrapper<SysSupportItem>()
                        .eq(SysSupportItem::getStatus, "pending"));
                long pendingChanges = changeService.count(new LambdaQueryWrapper<SysChange>()
                        .eq(SysChange::getStatus, "pending"));
                long pendingReview = reportService.count(new LambdaQueryWrapper<SysStageReport>()
                        .eq(SysStageReport::getReviewStatus, "pending"));

                result.put("openDeviations", openDeviations);
                result.put("pendingSupports", pendingSupports);
                result.put("pendingChanges", pendingChanges);
                result.put("pendingReview", pendingReview);
                break;
            }
            case "admin": {
                long userCount = userMapper.selectCount(null);
                long projectCount = projectMapper.selectCount(null);
                long activeProjectCount = projectMapper.selectCount(new LambdaQueryWrapper<SysProject>()
                        .eq(SysProject::getStatus, "active"));
                long completedProjectCount = projectMapper.selectCount(new LambdaQueryWrapper<SysProject>()
                        .eq(SysProject::getStatus, "completed"));
                long pendingReview = reportService.count(new LambdaQueryWrapper<SysStageReport>()
                        .eq(SysStageReport::getReviewStatus, "pending"));
                long openDeviations = deviationService.count(new LambdaQueryWrapper<SysDeviation>()
                        .eq(SysDeviation::getStatus, "open"));
                long pendingSupports = supportItemService.count(new LambdaQueryWrapper<SysSupportItem>()
                        .eq(SysSupportItem::getStatus, "pending"));
                long oaImportCount = projectMapper.selectCount(new LambdaQueryWrapper<SysProject>()
                        .likeRight(SysProject::getDescription, "OA导入项目"));
                List<SysProject> latestOaProjects = projectMapper.selectList(new LambdaQueryWrapper<SysProject>()
                        .likeRight(SysProject::getDescription, "OA导入项目")
                        .orderByDesc(SysProject::getCreateTime)
                        .last("LIMIT 1"));

                result.put("userCount", userCount);
                result.put("projectCount", projectCount);
                result.put("activeProjectCount", activeProjectCount);
                result.put("completedProjectCount", completedProjectCount);
                result.put("pendingReview", pendingReview);
                result.put("openDeviations", openDeviations);
                result.put("pendingSupports", pendingSupports);
                result.put("oaImportCount", oaImportCount);
                result.put("lastOaImportTime", latestOaProjects.isEmpty() ? null : latestOaProjects.get(0).getCreateTime());
                break;
            }
            default: {
                result.put("message", "no dashboard for role: " + role);
            }
        }

        return Result.ok(result);
    }

    private List<Map<String, Object>> buildProjectCards(List<Long> projectIds, Long userId) {
        if (projectIds == null || projectIds.isEmpty()) return Collections.emptyList();
        List<SysProject> projects = projectMapper.selectBatchIds(projectIds);
        // Get all open deviations for these projects
        Set<Long> devProjectIds = deviationService.listByProject(null).stream()
                .filter(d -> "open".equals(d.getStatus()))
                .filter(d -> projectIds.contains(d.getProjectId()))
                .map(SysDeviation::getProjectId)
                .collect(Collectors.toSet());
        return projects.stream().limit(8).map(p -> {
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("id", p.getId());
            card.put("name", p.getName());
            card.put("projectId", p.getId());
            card.put("projectName", p.getName());
            card.put("status", p.getStatus());
            // Find current active stage (first non-completed, ordered by sortOrder)
            List<SysProjectStage> stages = stageService.list(
                    new LambdaQueryWrapper<SysProjectStage>()
                            .eq(SysProjectStage::getProjectId, p.getId())
                            .orderByAsc(SysProjectStage::getSortOrder));
            SysProjectStage currentStage = stages.stream()
                    .filter(s -> !"completed".equals(s.getStatus()))
                    .findFirst().orElse(null);
            String currentStageText;
            if (currentStage != null) {
                currentStageText = currentStage.getStageName();
            } else if ("completed".equals(p.getStatus())) {
                currentStageText = "项目已完成";
            } else if (stages.isEmpty()) {
                currentStageText = "待负责人安排阶段";
            } else {
                currentStageText = "阶段已完成，待确认项目完成";
            }
            card.put("currentStage", currentStageText);
            card.put("stagePlanEnd", currentStage != null ? currentStage.getPlanEnd() : null);
            card.put("hasDeviation", devProjectIds.contains(p.getId()));
            return card;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildEngineerPendingStages(Long userId) {
        List<SysProjectStage> stages = stageService.list(
                new LambdaQueryWrapper<SysProjectStage>()
                        .eq(SysProjectStage::getAssigneeId, userId)
                        .ne(SysProjectStage::getStatus, "completed")
                        .orderByAsc(SysProjectStage::getPlanEnd)
                        .last("LIMIT 5"));
        // Batch-load project names for these stages
        Set<Long> pIds = stages.stream().map(SysProjectStage::getProjectId).collect(Collectors.toSet());
        Map<Long, String> nameMap = new HashMap<>();
        if (!pIds.isEmpty()) {
            projectMapper.selectBatchIds(pIds)
                    .forEach(p -> nameMap.put(p.getId(), p.getName()));
        }
        return stages.stream().map(s -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stageId", s.getId());
            item.put("projectId", s.getProjectId());
            item.put("stageName", s.getStageName());
            item.put("projectName", nameMap.getOrDefault(s.getProjectId(), ""));
            item.put("planEnd", s.getPlanEnd());
            item.put("status", s.getStatus());
            return item;
        }).collect(Collectors.toList());
    }
}
