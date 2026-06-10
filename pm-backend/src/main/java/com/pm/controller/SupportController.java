package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysSupportItem;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysSupportItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supports")
@RequiredArgsConstructor
public class SupportController {

    private final SysSupportItemService supportItemService;
    private final ProjectAccessService accessService;

    @GetMapping
    public Result<List<SysSupportItem>> list(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal LoginUser loginUser) {
        List<SysSupportItem> list = supportItemService.listAll(status);
        if (!accessService.isAdmin(loginUser.getUser()) && !accessService.isLeader(loginUser.getUser())) {
            Long userId = loginUser.getUser().getId();
            list.removeIf(item ->
                !userId.equals(item.getApplicantId()) &&
                !accessService.canViewProject(item.getProjectId(), loginUser.getUser()));
        }
        return Result.ok(list);
    }

    @PostMapping
    public Result<SysSupportItem> create(@RequestBody SysSupportItem item,
                                          @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectView(item.getProjectId(), loginUser.getUser());
        item.setApplicantId(loginUser.getUser().getId());
        item.setStatus("pending");
        supportItemService.save(item);
        return Result.ok(item);
    }

    @PutMapping("/{id}/resolve")
    public Result<?> resolve(@PathVariable Long id,
                             @RequestBody Map<String, String> body,
                             @AuthenticationPrincipal LoginUser loginUser) {
        SysSupportItem item = supportItemService.getById(id);
        if (item == null) {
            return Result.fail("支持事项不存在");
        }
        if (!accessService.isAdmin(loginUser.getUser()) && !accessService.isLeader(loginUser.getUser())) {
            accessService.requireProjectManager(item.getProjectId(), loginUser.getUser());
        }
        String reply = body.get("reply");
        String resolveNote = body.get("resolveNote");
        supportItemService.resolve(id, reply, resolveNote);
        return Result.ok();
    }
}
