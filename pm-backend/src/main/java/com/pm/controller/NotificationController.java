package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.common.Result;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.entity.SysUser;
import com.pm.mapper.SysProjectMapper;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysChangeService;
import com.pm.service.SysDeviationService;
import com.pm.service.SysProjectStageService;
import com.pm.service.SysStageReportService;
import com.pm.service.SysSupportItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final SysProjectStageService stageService;
    private final SysStageReportService reportService;
    private final SysDeviationService deviationService;
    private final SysSupportItemService supportService;
    private final SysChangeService changeService;
    private final ProjectAccessService accessService;
    private final SysProjectMapper projectMapper;

    private String fmt(LocalDateTime dt) {
        return dt != null ? dt.format(FMT) : "";
    }

    @GetMapping("/notifications")
    public Result<List<Map<String, Object>>> list(@AuthenticationPrincipal LoginUser loginUser) {
        SysUser user = loginUser.getUser();
        List<Map<String, Object>> list = new ArrayList<>();
        String role = user.getRole();

        if ("engineer".equals(role)) {
            Map<Long, SysStageReport> latestReturnedByStage = new LinkedHashMap<>();
            reportService.list(new LambdaQueryWrapper<SysStageReport>()
                    .eq(SysStageReport::getSubmitUserId, user.getId())
                    .eq(SysStageReport::getReviewStatus, "returned")
                    .orderByDesc(SysStageReport::getReviewTime)
                    .orderByDesc(SysStageReport::getSubmitTime))
                .forEach(report -> latestReturnedByStage.putIfAbsent(report.getStageId(), report));

            latestReturnedByStage.values().forEach(report -> {
                SysProjectStage stage = stageService.getById(report.getStageId());
                if (stage != null) {
                    SysProject project = projectMapper.selectById(stage.getProjectId());
                    String projectName = project != null ? project.getName() : "";
                    list.add(Map.of(
                            "type", "returned",
                            "message", projectName + "｜" + stage.getStageName() + "阶段已退回，需重新填报",
                            "url", "/my-tasks/" + stage.getId() + "/report",
                            "time", fmt(report.getReviewTime()),
                            "projectName", projectName
                    ));
                }
            });

            stageService.listMyTasks(user.getId()).stream()
                    .filter(s -> s.getPlanEnd() != null
                            && s.getPlanEnd().isBefore(LocalDate.now())
                            && !"completed".equals(s.getStatus()))
                    .forEach(s -> {
                        SysProject project = projectMapper.selectById(s.getProjectId());
                        String projectName = project != null ? project.getName() : "";
                        long days = ChronoUnit.DAYS.between(s.getPlanEnd(), LocalDate.now());
                        list.add(Map.of(
                                "type", "overdue",
                                "message", projectName + "｜" + s.getStageName() + "阶段已逾期 " + days + " 天",
                                "url", "/my-tasks/" + s.getId() + "/report",
                                "time", s.getPlanEnd().toString(),
                                "projectName", projectName
                        ));
                    });
        }

        if ("manager".equals(role)) {
            long pendingReview = reportService.listPendingReview(user.getId(), user.getRole()).size();
            if (pendingReview > 0) {
                list.add(Map.of(
                        "type", "review",
                        "message", pendingReview + " 条填报待审阅",
                        "url", "/pending-review",
                        "time", fmt(LocalDateTime.now()),
                        "projectName", "全院"
                ));
            }

            long pendingAchievement = reportService.listPendingReview(user.getId(), user.getRole()).stream()
                    .filter(r -> r.getAttachmentName() != null)
                    .count();
            if (pendingAchievement > 0) {
                list.add(Map.of(
                        "type", "achievement",
                        "message", pendingAchievement + " 条成果待审核",
                        "url", "/pending-review",
                        "time", fmt(LocalDateTime.now()),
                        "projectName", "全院"
                ));
            }

            long openDev = deviationService.listByProject(null).stream()
                    .filter(d -> "open".equals(d.getStatus()))
                    .filter(d -> accessService.canViewProject(d.getProjectId(), user))
                    .count();
            if (openDev > 0) {
                list.add(Map.of(
                        "type", "deviation",
                        "message", openDev + " 项偏差未关闭",
                        "url", "/deviations",
                        "time", fmt(LocalDateTime.now()),
                        "projectName", "全院"
                ));
            }

            long pendingSup = supportService.listAll("pending").stream()
                    .filter(s -> accessService.canViewProject(s.getProjectId(), user)
                            || user.getId().equals(s.getApplicantId()))
                    .count();
            if (pendingSup > 0) {
                list.add(Map.of(
                        "type", "support",
                        "message", pendingSup + " 项支持事项待处理",
                        "url", "/supports",
                        "time", fmt(LocalDateTime.now()),
                        "projectName", "全院"
                ));
            }
        }

        if ("leader".equals(role)) {
            long openDev = deviationService.listByProject(null).stream()
                    .filter(d -> "open".equals(d.getStatus()))
                    .count();
            if (openDev > 0) {
                list.add(Map.of(
                        "type", "deviation",
                        "message", openDev + " 项偏差未关闭",
                        "url", "/deviations",
                        "time", fmt(LocalDateTime.now()),
                        "projectName", "全院"
                ));
            }

            long pendingSup = supportService.listAll("pending").size();
            if (pendingSup > 0) {
                list.add(Map.of(
                        "type", "support",
                        "message", pendingSup + " 项支持事项待处理",
                        "url", "/supports",
                        "time", fmt(LocalDateTime.now()),
                        "projectName", "全院"
                ));
            }

            long pendingChanges = changeService.list().stream()
                    .filter(c -> "pending".equals(c.getStatus()))
                    .count();
            if (pendingChanges > 0) {
                list.add(Map.of(
                        "type", "change",
                        "message", pendingChanges + " 项变更待确认",
                        "url", "/leader-dashboard",
                        "time", fmt(LocalDateTime.now()),
                        "projectName", "全院"
                ));
            }
        }

        return Result.ok(list);
    }
}
