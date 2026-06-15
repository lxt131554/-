package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.common.Result;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.entity.SysUser;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final SysProjectStageService stageService;
    private final SysStageReportService reportService;
    private final SysDeviationService deviationService;
    private final SysSupportItemService supportService;
    private final SysChangeService changeService;
    private final ProjectAccessService accessService;

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
                    list.add(Map.of(
                            "type", "returned",
                            "message", "Stage [" + stage.getStageName() + "] was returned and needs resubmission",
                            "url", "/my-tasks/" + stage.getId() + "/report",
                            "time", report.getReviewTime() != null ? report.getReviewTime().toString() : ""
                    ));
                }
            });

            stageService.listMyTasks(user.getId()).stream()
                    .filter(s -> s.getPlanEnd() != null
                            && s.getPlanEnd().isBefore(LocalDate.now())
                            && !"completed".equals(s.getStatus()))
                    .forEach(s -> list.add(Map.of(
                            "type", "overdue",
                            "message", "Stage [" + s.getStageName() + "] is overdue by "
                                    + ChronoUnit.DAYS.between(s.getPlanEnd(), LocalDate.now()) + " day(s)",
                            "url", "/my-tasks/" + s.getId() + "/report",
                            "time", s.getPlanEnd().toString()
                    )));
        }

        if ("manager".equals(role)) {
            long pendingReview = reportService.listPendingReview(user.getId(), user.getRole()).size();
            if (pendingReview > 0) {
                list.add(Map.of(
                        "type", "review",
                        "message", pendingReview + " stage report(s) pending review",
                        "url", "/pending-review",
                        "time", LocalDateTime.now().toString()
                ));
            }

            long pendingAchievement = reportService.listPendingReview(user.getId(), user.getRole()).stream()
                    .filter(r -> r.getAttachmentName() != null)
                    .count();
            if (pendingAchievement > 0) {
                list.add(Map.of(
                        "type", "achievement",
                        "message", pendingAchievement + " deliverable review(s) pending",
                        "url", "/pending-review",
                        "time", LocalDateTime.now().toString()
                ));
            }

            long openDev = deviationService.listByProject(null).stream()
                    .filter(d -> "open".equals(d.getStatus()))
                    .filter(d -> accessService.canViewProject(d.getProjectId(), user))
                    .count();
            if (openDev > 0) {
                list.add(Map.of(
                        "type", "deviation",
                        "message", openDev + " deviation(s) still open",
                        "url", "/deviations",
                        "time", LocalDateTime.now().toString()
                ));
            }

            long pendingSup = supportService.listAll("pending").stream()
                    .filter(s -> accessService.canViewProject(s.getProjectId(), user)
                            || user.getId().equals(s.getApplicantId()))
                    .count();
            if (pendingSup > 0) {
                list.add(Map.of(
                        "type", "support",
                        "message", pendingSup + " support item(s) pending",
                        "url", "/supports",
                        "time", LocalDateTime.now().toString()
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
                        "message", openDev + " deviation(s) still open",
                        "url", "/deviations",
                        "time", LocalDateTime.now().toString()
                ));
            }

            long pendingSup = supportService.listAll("pending").size();
            if (pendingSup > 0) {
                list.add(Map.of(
                        "type", "support",
                        "message", pendingSup + " support item(s) pending",
                        "url", "/supports",
                        "time", LocalDateTime.now().toString()
                ));
            }

            long pendingChanges = changeService.list().stream()
                    .filter(c -> "pending".equals(c.getStatus()))
                    .count();
            if (pendingChanges > 0) {
                list.add(Map.of(
                        "type", "change",
                        "message", pendingChanges + " change request(s) pending confirmation",
                        "url", "/leader-dashboard",
                        "time", LocalDateTime.now().toString()
                ));
            }
        }

        return Result.ok(list);
    }
}
