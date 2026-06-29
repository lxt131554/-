package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pm.common.Result;
import com.pm.dto.OaProjectImportResult;
import com.pm.entity.SysApproval;
import com.pm.entity.SysChange;
import com.pm.entity.SysDeviation;
import com.pm.entity.SysExperience;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysReview;
import com.pm.entity.SysStageReport;
import com.pm.entity.SysSupportItem;
import com.pm.entity.SysUser;
import com.pm.mapper.SysApprovalMapper;
import com.pm.mapper.SysChangeMapper;
import com.pm.mapper.SysDeviationMapper;
import com.pm.mapper.SysExperienceMapper;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysProjectStageMapper;
import com.pm.mapper.SysReviewMapper;
import com.pm.mapper.SysStageReportMapper;
import com.pm.mapper.SysSupportItemMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.security.LoginUser;
import com.pm.service.OaProjectImportService;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysDeviationService;
import com.pm.service.SysProjectService;
import com.pm.service.SysSupportItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    private final SysProjectStageMapper stageMapper;
    private final SysStageReportMapper reportMapper;
    private final SysDeviationMapper deviationMapper;
    private final SysSupportItemMapper supportItemMapper;
    private final SysChangeMapper changeMapper;
    private final SysReviewMapper reviewMapper;
    private final SysExperienceMapper experienceMapper;
    private final SysApprovalMapper approvalMapper;
    private final SysDeviationService deviationService;
    private final SysSupportItemService supportItemService;
    private final OaProjectImportService oaProjectImportService;
    private final ProjectAccessService accessService;

    @GetMapping
    public Result<IPage<SysProject>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (size > 100) size = 100;
        SysUser user = loginUser.getUser();
        IPage<SysProject> result = projectService.pageWithMembers(
                page, size, keyword, status, user.getId(), user.getRole());
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<SysProject> detail(@PathVariable Long id,
                                     @AuthenticationPrincipal LoginUser loginUser) {
        SysProject project = projectService.getById(id);
        if (project == null) {
            return Result.fail(404, "项目不存在");
        }
        accessService.requireProjectView(id, loginUser.getUser());
        List<SysDeviation> deviations = deviationService.listByProject(id);
        List<SysSupportItem> supportItems = supportItemService.listByProject(id);
        project.setDeviations(deviations);
        project.setSupportItems(supportItems);
        project.setSupports(supportItems);
        return Result.ok(project);
    }

    @PostMapping
    public Result<SysProject> create(@Valid @RequestBody SysProject project,
                                     @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectCreator(loginUser.getUser());
        // 校验项目名称重复
        long nameCount = projectService.count(new LambdaQueryWrapper<SysProject>()
                .eq(SysProject::getName, project.getName()));
        if (nameCount > 0) {
            return Result.fail(400, "项目名称已存在，请使用不同的名称");
        }
        project.setCreateUserId(loginUser.getUser().getId());
        project.setStatus("active");
        projectService.save(project);
        projectService.addMember(project.getId(), loginUser.getUser().getId(), "manager", "confirmed");
        return Result.ok(project);
    }

    @PostMapping("/import/oa")
    public Result<OaProjectImportResult> importFromOa(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireAdmin(loginUser.getUser());
        return Result.ok(oaProjectImportService.importProjects(file, loginUser.getUser().getId()));
    }

    @PutMapping("/{id}")
    public Result<SysProject> update(@PathVariable Long id, @Valid @RequestBody SysProject project,
                                     @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(id, loginUser.getUser());
        accessService.requireProjectActive(id);
        project.setId(id);
        projectService.updateById(project);
        return Result.ok(project);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id,
                            @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireAdmin(loginUser.getUser());
        // 级联清理子表数据，避免孤儿记录
        memberMapper.delete(new LambdaQueryWrapper<SysProjectMember>().eq(SysProjectMember::getProjectId, id));
        stageMapper.delete(new LambdaQueryWrapper<SysProjectStage>().eq(SysProjectStage::getProjectId, id));
        reportMapper.delete(new LambdaQueryWrapper<SysStageReport>().eq(SysStageReport::getProjectId, id));
        deviationMapper.delete(new LambdaQueryWrapper<SysDeviation>().eq(SysDeviation::getProjectId, id));
        supportItemMapper.delete(new LambdaQueryWrapper<SysSupportItem>().eq(SysSupportItem::getProjectId, id));
        changeMapper.delete(new LambdaQueryWrapper<SysChange>().eq(SysChange::getProjectId, id));
        reviewMapper.delete(new LambdaQueryWrapper<SysReview>().eq(SysReview::getProjectId, id));
        experienceMapper.delete(new LambdaQueryWrapper<SysExperience>().eq(SysExperience::getProjectId, id));
        approvalMapper.delete(new LambdaQueryWrapper<SysApproval>().eq(SysApproval::getProjectId, id));
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
            Map<String, Object> info = new HashMap<>();
            info.put("id", m.getId());
            info.put("userId", m.getUserId());
            info.put("realName", u != null ? u.getRealName() : "");
            info.put("dept", u != null ? u.getDept() : "");
            info.put("roleInProject", m.getRoleInProject());
            info.put("status", m.getStatus());
            list.add(info);
        }
        return Result.ok(list);
    }

    @PostMapping("/{id}/members")
    public Result<?> addMember(@PathVariable Long id, @RequestBody Map<String, Object> body,
                               @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(id, loginUser.getUser());
        Object userIdValue = body.get("userId");
        Object roleValue = body.get("roleInProject");
        if (userIdValue == null || roleValue == null) {
            throw new IllegalArgumentException("请选择成员和项目角色");
        }
        Long userId;
        try {
            userId = Long.valueOf(userIdValue.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("成员ID格式错误");
        }
        String roleInProject = roleValue.toString();
        if (!"manager".equals(roleInProject) && !"engineer".equals(roleInProject)) {
            throw new IllegalArgumentException("项目角色只能是负责人或工程师");
        }
        projectService.addMember(id, userId, roleInProject, "pending");
        return Result.ok();
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public Result<?> removeMember(@PathVariable Long id, @PathVariable Long memberId,
                                  @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(id, loginUser.getUser());
        SysProjectMember member = memberMapper.selectById(memberId);
        if (member == null || !member.getProjectId().equals(id)) {
            return Result.fail("成员不属于该项目");
        }
        projectService.removeMember(id, memberId);
        return Result.ok();
    }

    @PutMapping("/{projectId}/members/{memberId}/confirm")
    public Result<?> confirmMember(@PathVariable Long projectId, @PathVariable Long memberId,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        SysProjectMember member = memberMapper.selectById(memberId);
        if (member == null
                || !projectId.equals(member.getProjectId())
                || !member.getUserId().equals(loginUser.getUser().getId())) {
            return Result.fail("forbidden");
        }
        member.setStatus("confirmed");
        memberMapper.updateById(member);
        return Result.ok();
    }

    @PutMapping("/{projectId}/members/{memberId}/reject")
    public Result<?> rejectMember(@PathVariable Long projectId, @PathVariable Long memberId,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        SysProjectMember member = memberMapper.selectById(memberId);
        if (member == null
                || !projectId.equals(member.getProjectId())
                || !member.getUserId().equals(loginUser.getUser().getId())) {
            return Result.fail("无权操作");
        }
        if (!"pending".equals(member.getStatus())) {
            return Result.fail("只能拒绝待确认的邀请");
        }
        memberMapper.deleteById(memberId);
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

    @PutMapping("/{id}/complete")
    public Result<?> complete(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireProjectManager(id, loginUser.getUser());

        List<String> reasons = new ArrayList<>();

        // Check incomplete stages
        long incompleteStages = stageMapper.selectCount(
            new LambdaQueryWrapper<SysProjectStage>()
                .eq(SysProjectStage::getProjectId, id)
                .ne(SysProjectStage::getStatus, "completed"));
        if (incompleteStages > 0) {
            reasons.add(incompleteStages + " 个阶段尚未完成");
        }

        // Check pending reports
        long pendingReports = reportMapper.selectCount(
            new LambdaQueryWrapper<SysStageReport>()
                .eq(SysStageReport::getProjectId, id)
                .ne(SysStageReport::getReviewStatus, "passed"));
        if (pendingReports > 0) {
            reasons.add(pendingReports + " 条填报尚未审核通过");
        }

        // Check open deviations
        long openDeviations = deviationMapper.selectCount(
            new LambdaQueryWrapper<SysDeviation>()
                .eq(SysDeviation::getProjectId, id)
                .eq(SysDeviation::getStatus, "open"));
        if (openDeviations > 0) {
            reasons.add(openDeviations + " 项偏差尚未关闭");
        }

        // Check pending supports
        long pendingSupports = supportItemMapper.selectCount(
            new LambdaQueryWrapper<SysSupportItem>()
                .eq(SysSupportItem::getProjectId, id)
                .eq(SysSupportItem::getStatus, "pending"));
        if (pendingSupports > 0) {
            reasons.add(pendingSupports + " 项支持事项尚未处理");
        }

        // Check pending changes
        long pendingChanges = changeMapper.selectCount(
            new LambdaQueryWrapper<SysChange>()
                .eq(SysChange::getProjectId, id)
                .eq(SysChange::getStatus, "pending"));
        if (pendingChanges > 0) {
            reasons.add(pendingChanges + " 项变更尚未确认");
        }

        if (!reasons.isEmpty()) {
            return Result.fail(400, "项目不满足结项条件：\n" + String.join("\n", reasons));
        }

        SysProject project = projectService.getById(id);
        project.setStatus("completed");
        projectService.updateById(project);

        return Result.ok(project);
    }

    @PutMapping("/{id}/reopen")
    public Result<?> reopen(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireAdmin(loginUser.getUser());
        SysProject project = projectService.getById(id);
        if (project == null || !"completed".equals(project.getStatus())) {
            return Result.fail("只能重新打开已完成的项目");
        }
        project.setStatus("active");
        projectService.updateById(project);
        return Result.ok(project);
    }
}
