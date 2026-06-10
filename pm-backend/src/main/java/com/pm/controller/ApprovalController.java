package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysApproval;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ApprovalController {

    private final SysApprovalService approvalService;
    private final ProjectAccessService accessService;

    @GetMapping("/api/projects/{projectId}/approval")
    public Result<SysApproval> getByProject(@PathVariable Long projectId,
                                            @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectView(projectId, loginUser.getUser());
        SysApproval approval = approvalService.getByProject(projectId);
        return Result.ok(approval);
    }

    @PostMapping("/api/projects/{projectId}/approval")
    public Result<?> saveOrUpdate(@PathVariable Long projectId,
                                  @RequestBody SysApproval approval,
                                  @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(projectId, loginUser.getUser());
        approvalService.saveOrUpdate(projectId, approval, loginUser.getUser().getId());
        return Result.ok();
    }
}
