package com.pm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.common.Result;
import com.pm.entity.SysSupportItem;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysSupportItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supports")
@RequiredArgsConstructor
public class SupportController {

    private final SysSupportItemService supportItemService;
    private final ProjectAccessService accessService;

    @GetMapping
    public Result<Page<SysSupportItem>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (size > 100) size = 100;
        List<SysSupportItem> list = supportItemService.listAll(status);
        if (!accessService.isAdmin(loginUser.getUser()) && !accessService.isLeader(loginUser.getUser())) {
            Long userId = loginUser.getUser().getId();
            list.removeIf(item ->
                !userId.equals(item.getApplicantId()) &&
                !accessService.canViewProject(item.getProjectId(), loginUser.getUser()));
        }
        long total = list.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, (int) total);
        Page<SysSupportItem> p = new Page<>(page, size, total);
        p.setRecords(start < total ? list.subList(start, end) : Collections.emptyList());
        return Result.ok(p);
    }

    @PostMapping
    public Result<SysSupportItem> create(@Valid @RequestBody SysSupportItem item,
                                          @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(item.getProjectId(), loginUser.getUser());
        accessService.requireProjectActive(item.getProjectId());
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
        if (!hasText(reply)) {
            throw new IllegalArgumentException("处理回复不能为空");
        }
        supportItemService.resolve(id, reply, resolveNote);
        return Result.ok();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
