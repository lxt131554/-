package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.*;
import com.pm.security.LoginUser;
import com.pm.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
            // Returned reports (need re-submit)
            stageService.listMyTasks(user.getId()).stream()
                .filter(s -> "in_progress".equals(s.getStatus()))
                .forEach(s -> list.add(Map.of(
                    "type", "returned", "message", "「" + s.getStageName() + "」被退回，需重新填报",
                    "url", "/my-tasks/" + s.getId() + "/report", "time", s.getUpdateTime() != null ? s.getUpdateTime().toString() : ""
                )));
            // Overdue stages
            stageService.listMyTasks(user.getId()).stream()
                .filter(s -> s.getPlanEnd() != null && s.getPlanEnd().isBefore(LocalDate.now()) && !"completed".equals(s.getStatus()))
                .forEach(s -> list.add(Map.of(
                    "type", "overdue", "message", "「" + s.getStageName() + "」已逾期 " +
                        ChronoUnit.DAYS.between(s.getPlanEnd(), LocalDate.now()) + " 天",
                    "url", "/my-tasks/" + s.getId() + "/report", "time", s.getPlanEnd().toString()
                )));
        }

        if ("manager".equals(role)) {
            // Pending reviews
            long pendingReview = reportService.listPendingReview(user.getId(), user.getRole()).size();
            if (pendingReview > 0) {
                list.add(Map.of("type", "review", "message", pendingReview + " 条阶段填报待审阅",
                    "url", "/pending-review", "time", LocalDateTime.now().toString()));
            }
            // Pending achievements
            long pendingAchievement = reportService.listPendingReview(user.getId(), user.getRole()).stream()
                .filter(r -> r.getAttachmentName() != null).count();
            if (pendingAchievement > 0) {
                list.add(Map.of("type", "achievement", "message", pendingAchievement + " 项成果待审核",
                    "url", "/pending-review", "time", LocalDateTime.now().toString()));
            }
            // Open deviations
            long openDev = deviationService.listByProject(null).stream()
                .filter(d -> "open".equals(d.getStatus()))
                .filter(d -> accessService.canViewProject(d.getProjectId(), user))
                .count();
            if (openDev > 0) {
                list.add(Map.of("type", "deviation", "message", openDev + " 项偏差未关闭",
                    "url", "/deviations", "time", LocalDateTime.now().toString()));
            }
            // Pending supports
            long pendingSup = supportService.listAll("pending").stream()
                .filter(s -> accessService.canViewProject(s.getProjectId(), user) || user.getId().equals(s.getApplicantId()))
                .count();
            if (pendingSup > 0) {
                list.add(Map.of("type", "support", "message", pendingSup + " 项支持事项待处理",
                    "url", "/supports", "time", LocalDateTime.now().toString()));
            }
        }

        if ("leader".equals(role)) {
            long openDev = deviationService.listByProject(null).stream()
                .filter(d -> "open".equals(d.getStatus())).count();
            if (openDev > 0) {
                list.add(Map.of("type", "deviation", "message", openDev + " 项偏差未关闭",
                    "url", "/deviations", "time", LocalDateTime.now().toString()));
            }
            long pendingSup = supportService.listAll("pending").size();
            if (pendingSup > 0) {
                list.add(Map.of("type", "support", "message", pendingSup + " 项支持事项待处理",
                    "url", "/supports", "time", LocalDateTime.now().toString()));
            }
            // Pending changes to confirm
            long pendingChanges = changeService.list().stream()
                .filter(c -> "pending".equals(c.getStatus())).count();
            if (pendingChanges > 0) {
                list.add(Map.of("type", "change", "message", pendingChanges + " 项变更待确认",
                    "url", "/leader-dashboard", "time", LocalDateTime.now().toString()));
            }
        }

        return Result.ok(list);
    }
}
