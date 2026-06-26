package com.pm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.entity.SysUser;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysProjectStageMapper;
import com.pm.mapper.SysStageReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    private final SysProjectMapper projectMapper;
    private final SysProjectMemberMapper memberMapper;
    private final SysProjectStageMapper stageMapper;
    private final SysStageReportMapper reportMapper;

    public boolean isAdmin(SysUser user) {
        return user != null && "admin".equals(user.getRole());
    }

    public boolean isLeader(SysUser user) {
        return user != null && "leader".equals(user.getRole());
    }

    public boolean isManagerRole(SysUser user) {
        return user != null && "manager".equals(user.getRole());
    }

    public boolean canCreateProject(SysUser user) {
        return isAdmin(user) || isManagerRole(user);
    }

    public boolean canViewProject(Long projectId, SysUser user) {
        if (projectId == null || user == null) return false;
        if (isAdmin(user) || isLeader(user)) return true;
        return isConfirmedProjectMember(projectId, user.getId());
    }

    public boolean canManageProject(Long projectId, SysUser user) {
        if (projectId == null || user == null) return false;
        if (isAdmin(user)) return true;
        return isManagerRole(user) && isConfirmedProjectManager(projectId, user.getId());
    }

    public boolean canViewStage(Long stageId, SysUser user) {
        SysProjectStage stage = stageMapper.selectById(stageId);
        return stage != null && canViewProject(stage.getProjectId(), user);
    }

    public boolean canManageStage(Long stageId, SysUser user) {
        SysProjectStage stage = stageMapper.selectById(stageId);
        return stage != null && canManageProject(stage.getProjectId(), user);
    }

    public boolean canReportStage(Long stageId, SysUser user) {
        SysProjectStage stage = stageMapper.selectById(stageId);
        if (stage == null || user == null) return false;
        // 仅待填报/进行中/被退回阶段可填报，已提交和已完成不可再填报
        if ("submitted".equals(stage.getStatus()) || "completed".equals(stage.getStatus())) return false;
        if (isAdmin(user)) return true;
        if (stage.getAssigneeId() != null && stage.getAssigneeId().equals(user.getId())) return true;
        return canManageProject(stage.getProjectId(), user);
    }

    public boolean canViewReport(Long reportId, SysUser user) {
        SysStageReport report = reportMapper.selectById(reportId);
        return report != null && canViewProject(report.getProjectId(), user);
    }

    public boolean canReviewReport(Long reportId, SysUser user) {
        SysStageReport report = reportMapper.selectById(reportId);
        return report != null && canManageProject(report.getProjectId(), user);
    }

    public void requireAdmin(SysUser user) {
        if (!isAdmin(user)) deny();
    }

    public void requireLeaderOrAdmin(SysUser user) {
        if (!isLeader(user) && !isAdmin(user)) deny();
    }

    public void requireProjectCreator(SysUser user) {
        if (!canCreateProject(user)) deny();
    }

    public void requireProjectView(Long projectId, SysUser user) {
        if (!canViewProject(projectId, user)) deny();
    }

    public void requireProjectManager(Long projectId, SysUser user) {
        if (!canManageProject(projectId, user)) deny();
    }

    public void requireProjectActive(Long projectId) {
        SysProject project = projectMapper.selectById(projectId);
        if (project != null && "completed".equals(project.getStatus())) {
            throw new AccessDeniedException("已完成项目不允许修改阶段、成员或填报");
        }
    }

    public void requireStageView(Long stageId, SysUser user) {
        if (!canViewStage(stageId, user)) deny();
    }

    public void requireStageManager(Long stageId, SysUser user) {
        if (!canManageStage(stageId, user)) deny();
    }

    public void requireStageReport(Long stageId, SysUser user) {
        if (!canReportStage(stageId, user)) deny();
    }

    public void requireReportView(Long reportId, SysUser user) {
        if (!canViewReport(reportId, user)) deny();
    }

    public void requireReportReview(Long reportId, SysUser user) {
        if (!canReviewReport(reportId, user)) deny();
    }

    private boolean isConfirmedProjectMember(Long projectId, Long userId) {
        Long count = memberMapper.selectCount(new LambdaQueryWrapper<SysProjectMember>()
                .eq(SysProjectMember::getProjectId, projectId)
                .eq(SysProjectMember::getUserId, userId)
                .eq(SysProjectMember::getStatus, "confirmed"));
        return count != null && count > 0;
    }

    private boolean isConfirmedProjectManager(Long projectId, Long userId) {
        Long count = memberMapper.selectCount(new LambdaQueryWrapper<SysProjectMember>()
                .eq(SysProjectMember::getProjectId, projectId)
                .eq(SysProjectMember::getUserId, userId)
                .eq(SysProjectMember::getRoleInProject, "manager")
                .eq(SysProjectMember::getStatus, "confirmed"));
        return count != null && count > 0;
    }

    private void deny() {
        throw new AccessDeniedException("无权操作该数据");
    }
}
