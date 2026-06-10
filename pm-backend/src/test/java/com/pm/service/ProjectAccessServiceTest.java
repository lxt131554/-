package com.pm.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.entity.SysUser;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysProjectStageMapper;
import com.pm.mapper.SysStageReportMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectAccessServiceTest {

    private final SysProjectMemberMapper memberMapper = mock(SysProjectMemberMapper.class);
    private final SysProjectStageMapper stageMapper = mock(SysProjectStageMapper.class);
    private final SysStageReportMapper reportMapper = mock(SysStageReportMapper.class);
    private final ProjectAccessService accessService =
            new ProjectAccessService(memberMapper, stageMapper, reportMapper);

    @Test
    void leaderCanViewButCannotManageProjects() {
        SysUser leader = user(4L, "leader");

        assertDoesNotThrow(() -> accessService.requireProjectView(100L, leader));
        assertThrows(AccessDeniedException.class,
                () -> accessService.requireProjectManager(100L, leader));
    }

    @Test
    void confirmedProjectManagerCanManageProject() {
        SysUser manager = user(2L, "manager");
        when(memberMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertDoesNotThrow(() -> accessService.requireProjectManager(100L, manager));
    }

    @Test
    void engineerCannotManageProjectEvenWhenMember() {
        SysUser engineer = user(3L, "engineer");
        when(memberMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertThrows(AccessDeniedException.class,
                () -> accessService.requireProjectManager(100L, engineer));
    }

    @Test
    void stageAssigneeCanSubmitReport() {
        SysUser engineer = user(3L, "engineer");
        SysProjectStage stage = new SysProjectStage();
        stage.setId(10L);
        stage.setProjectId(100L);
        stage.setAssigneeId(3L);
        when(stageMapper.selectById(10L)).thenReturn(stage);

        assertDoesNotThrow(() -> accessService.requireStageReport(10L, engineer));
    }

    @Test
    void projectManagerCanReviewProjectReport() {
        SysUser manager = user(2L, "manager");
        SysStageReport report = new SysStageReport();
        report.setId(20L);
        report.setProjectId(100L);
        when(reportMapper.selectById(20L)).thenReturn(report);
        when(memberMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertDoesNotThrow(() -> accessService.requireReportReview(20L, manager));
    }

    private SysUser user(Long id, String role) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
