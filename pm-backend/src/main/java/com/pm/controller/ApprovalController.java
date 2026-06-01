package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysApproval;
import com.pm.security.LoginUser;
import com.pm.service.SysApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ApprovalController {

    private final SysApprovalService approvalService;

    @GetMapping("/api/projects/{projectId}/approval")
    public Result<SysApproval> getByProject(@PathVariable Long projectId) {
        SysApproval approval = approvalService.getByProject(projectId);
        return Result.ok(approval);
    }

    @PostMapping("/api/projects/{projectId}/approval")
    public Result<?> saveOrUpdate(@PathVariable Long projectId,
                                  @RequestBody SysApproval approval,
                                  @AuthenticationPrincipal LoginUser loginUser) {
        approvalService.saveOrUpdate(projectId, approval, loginUser.getUser().getId());
        return Result.ok();
    }
}
