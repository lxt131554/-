package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.common.Result;
import com.pm.entity.SysSupportItem;
import com.pm.entity.SysProject;
import com.pm.entity.SysUser;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysSupportItemMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.CacheEvictionService;
import com.pm.service.SysSupportItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supports")
@RequiredArgsConstructor
public class SupportController {

    private final SysSupportItemService supportItemService;
    private final SysSupportItemMapper supportItemMapper;
    private final SysProjectMapper projectMapper;
    private final SysUserMapper userMapper;
    private final ProjectAccessService accessService;
    private final CacheEvictionService cacheEvictionService;

    @GetMapping
    public Result<Page<SysSupportItem>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (size > 100) size = 100;
        Page<SysSupportItem> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<SysSupportItem> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(SysSupportItem::getStatus, status);
        }
        if (!accessService.isAdmin(loginUser.getUser()) && !accessService.isLeader(loginUser.getUser())) {
            Long userId = loginUser.getUser().getId();
            List<Long> projectIds = accessService.listConfirmedProjectIds(loginUser.getUser());
            if (projectIds.isEmpty()) {
                wrapper.eq(SysSupportItem::getApplicantId, userId);
            } else {
                wrapper.and(scope -> scope
                        .in(SysSupportItem::getProjectId, projectIds)
                        .or()
                        .eq(SysSupportItem::getApplicantId, userId));
            }
        }
        wrapper.orderByDesc(SysSupportItem::getCreateTime);
        pageObj = supportItemMapper.selectPage(pageObj, wrapper);
        for (SysSupportItem item : pageObj.getRecords()) {
            enrichNames(item);
        }
        return Result.ok(pageObj);
    }

    @GetMapping("/{id}")
    public Result<SysSupportItem> detail(@PathVariable Long id,
                                         @AuthenticationPrincipal LoginUser loginUser) {
        SysSupportItem item = supportItemService.getById(id);
        if (item == null) {
            return Result.fail("支持事项不存在");
        }
        SysUser user = loginUser.getUser();
        Long userId = user.getId();
        if (!accessService.isAdmin(user)
                && !accessService.isLeader(user)
                && !userId.equals(item.getApplicantId())
                && !accessService.canViewProject(item.getProjectId(), user)) {
            throw new AccessDeniedException("无权操作该数据");
        }
        enrichNames(item);
        return Result.ok(item);
    }

    @PostMapping
    public Result<SysSupportItem> create(@Valid @RequestBody SysSupportItem item,
                                          @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectView(item.getProjectId(), loginUser.getUser());
        accessService.requireProjectActive(item.getProjectId());
        item.setApplicantId(loginUser.getUser().getId());
        item.setStatus("pending");
        supportItemService.save(item);
        enrichNames(item);
        cacheEvictionService.evictDashboardCaches();
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
        cacheEvictionService.evictDashboardCaches();
        return Result.ok();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void enrichNames(SysSupportItem item) {
        SysProject project = projectMapper.selectById(item.getProjectId());
        if (project != null) item.setProjectName(project.getName());
        SysUser applicant = userMapper.selectById(item.getApplicantId());
        if (applicant != null) item.setApplicantName(applicant.getRealName());
        if (item.getHandlerId() != null) {
            SysUser handler = userMapper.selectById(item.getHandlerId());
            if (handler != null) item.setHandlerName(handler.getRealName());
        }
    }
}
