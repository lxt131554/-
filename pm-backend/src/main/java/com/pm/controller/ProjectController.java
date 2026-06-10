package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pm.common.Result;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysUser;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final SysProjectService projectService;
    private final SysUserMapper userMapper;
    private final SysProjectMemberMapper memberMapper;
    private final ProjectAccessService accessService;

    @GetMapping
    public Result<IPage<SysProject>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal LoginUser loginUser) {
        SysUser user = loginUser.getUser();
        IPage<SysProject> result = projectService.pageWithMembers(page, size, keyword, status, user.getId(), user.getRole());
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<SysProject> detail(@PathVariable Long id,
                                     @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectView(id, loginUser.getUser());
        SysProject project = projectService.getById(id);
        if (project == null) {
            return Result.fail("项目不存在");
        }
        return Result.ok(project);
    }

    @PostMapping
    public Result<SysProject> create(@RequestBody SysProject project,
                                     @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectCreator(loginUser.getUser());
        project.setCreateUserId(loginUser.getUser().getId());
        project.setStatus("active");
        projectService.save(project);
        projectService.addMember(project.getId(), loginUser.getUser().getId(), "manager", "confirmed");
        return Result.ok(project);
    }

    @PutMapping("/{id}")
    public Result<SysProject> update(@PathVariable Long id, @RequestBody SysProject project,
                                     @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(id, loginUser.getUser());
        project.setId(id);
        projectService.updateById(project);
        return Result.ok(project);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id,
                            @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireAdmin(loginUser.getUser());
        projectService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/{id}/members")
    public Result<List<Map<String, Object>>> members(@PathVariable Long id,
                                                     @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectView(id, loginUser.getUser());
        List<SysProjectMember> members = projectService.getMembers(id);
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysProjectMember m : members) {
            SysUser u = userMapper.selectById(m.getUserId());
            Map<String, Object> info = new java.util.HashMap<>();
            info.put("id", m.getId());
            info.put("userId", m.getUserId());
            info.put("realName", u != null ? u.getRealName() : "");
            info.put("dept", u != null ? u.getDept() : "");
            info.put("roleInProject", m.getRoleInProject());
            list.add(info);
        }
        return Result.ok(list);
    }

    @PostMapping("/{id}/members")
    public Result<?> addMember(@PathVariable Long id, @RequestBody Map<String, Object> body,
                               @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(id, loginUser.getUser());
        Long userId = Long.valueOf(body.get("userId").toString());
        String roleInProject = body.get("roleInProject").toString();
        projectService.addMember(id, userId, roleInProject, "pending");
        return Result.ok();
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public Result<?> removeMember(@PathVariable Long id, @PathVariable Long memberId,
                                  @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(id, loginUser.getUser());
        projectService.removeMember(id, memberId);
        return Result.ok();
    }

    @PutMapping("/{projectId}/members/{memberId}/confirm")
    public Result<?> confirmMember(@PathVariable Long projectId, @PathVariable Long memberId,
                                    @AuthenticationPrincipal LoginUser loginUser) {
        SysProjectMember member = memberMapper.selectById(memberId);
        if (member == null || !member.getUserId().equals(loginUser.getUser().getId())) {
            return Result.fail("无权操作");
        }
        member.setStatus("confirmed");
        memberMapper.updateById(member);
        return Result.ok();
    }

    @GetMapping("/members/pending")
    public Result<List<Map<String, Object>>> pendingInvites(@AuthenticationPrincipal LoginUser loginUser) {
        List<SysProjectMember> list = memberMapper.selectList(
            new LambdaQueryWrapper<SysProjectMember>()
                .eq(SysProjectMember::getUserId, loginUser.getUser().getId())
                .eq(SysProjectMember::getStatus, "pending")
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysProjectMember m : list) {
            SysProject project = projectService.getById(m.getProjectId());
            Map<String, Object> info = new HashMap<>();
            info.put("id", m.getId());
            info.put("projectId", m.getProjectId());
            info.put("projectName", project != null ? project.getName() : "");
            info.put("roleInProject", m.getRoleInProject());
            result.add(info);
        }
        return Result.ok(result);
    }
}
