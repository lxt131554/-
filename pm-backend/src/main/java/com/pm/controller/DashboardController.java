package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.common.Result;
import com.pm.entity.SysDeviation;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.entity.SysSupportItem;
import com.pm.entity.SysChange;
import com.pm.security.LoginUser;
import com.pm.service.SysDeviationService;
import com.pm.service.SysProjectStageService;
import com.pm.service.SysStageReportService;
import com.pm.service.SysSupportItemService;
import com.pm.service.SysChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysProjectStageService stageService;
    private final SysStageReportService reportService;
    private final SysDeviationService deviationService;
    private final SysSupportItemService supportItemService;
    private final SysChangeService changeService;

    @GetMapping
    public Result<Map<String, Object>> stats(@AuthenticationPrincipal LoginUser loginUser) {
        String role = loginUser.getUser().getRole();
        Long userId = loginUser.getUser().getId();

        Map<String, Object> result = new LinkedHashMap<>();

        switch (role) {
            case "engineer": {
                long todo = stageService.count(new LambdaQueryWrapper<SysProjectStage>()
                        .eq(SysProjectStage::getAssigneeId, userId)
                        .ne(SysProjectStage::getStatus, "completed"));
                long returned = reportService.count(new LambdaQueryWrapper<SysStageReport>()
                        .eq(SysStageReport::getSubmitUserId, userId)
                        .eq(SysStageReport::getReviewStatus, "returned"));
                result.put("todo", todo);
                result.put("returned", returned);
                break;
            }
            case "manager": {
                long pendingReview = reportService.count(new LambdaQueryWrapper<SysStageReport>()
                        .eq(SysStageReport::getReviewStatus, "pending"));
                long pendingAchievement = stageService.count(new LambdaQueryWrapper<SysProjectStage>()
                        .eq(SysProjectStage::getStatus, "submitted"));
                long openDeviations = deviationService.count(new LambdaQueryWrapper<SysDeviation>()
                        .eq(SysDeviation::getStatus, "open"));
                long pendingSupports = supportItemService.count(new LambdaQueryWrapper<SysSupportItem>()
                        .eq(SysSupportItem::getStatus, "pending"));
                long pendingChanges = changeService.count(new LambdaQueryWrapper<SysChange>()
                        .eq(SysChange::getStatus, "pending"));
                result.put("pendingReview", pendingReview);
                result.put("pendingAchievement", pendingAchievement);
                result.put("openDeviations", openDeviations);
                result.put("pendingSupports", pendingSupports);
                result.put("pendingChanges", pendingChanges);
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
            default: {
                result.put("message", "no dashboard for role: " + role);
            }
        }

        return Result.ok(result);
    }
}
