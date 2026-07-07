package com.pm.service.impl;

import com.pm.entity.SysDeviation;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysReview;
import com.pm.entity.SysStageReport;
import com.pm.entity.SysSupportItem;
import com.pm.entity.SysUser;
import com.pm.mapper.SysStageReportMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.service.SysApprovalService;
import com.pm.service.SysChangeService;
import com.pm.service.SysDeviationService;
import com.pm.service.SysExperienceService;
import com.pm.service.SysProjectService;
import com.pm.service.SysProjectStageService;
import com.pm.service.SysReviewService;
import com.pm.service.SysSupportItemService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectExportServiceImplTest {

    @Mock private SysProjectService projectService;
    @Mock private SysProjectStageService stageService;
    @Mock private SysStageReportMapper stageReportMapper;
    @Mock private SysDeviationService deviationService;
    @Mock private SysChangeService changeService;
    @Mock private SysReviewService reviewService;
    @Mock private SysExperienceService experienceService;
    @Mock private SysApprovalService approvalService;
    @Mock private SysSupportItemService supportItemService;
    @Mock private SysUserMapper userMapper;

    @InjectMocks
    private ProjectExportServiceImpl exportService;

    @Test
    void exportsAllProjectPageDetailsInsteadOfOnlyLatestStageSummary() throws Exception {
        SysProject project = new SysProject();
        project.setId(1L);
        project.setName("森林资源调查项目");
        project.setDescription("项目页面中的完整项目描述");
        project.setStatus("active");
        project.setCustomerLevel("A级");
        project.setMainRisks("雨季外业进度风险");
        project.setHrAllocation("负责人1人，工程师2人");
        when(projectService.getById(1L)).thenReturn(project);

        SysProjectMember manager = new SysProjectMember();
        manager.setUserId(10L);
        manager.setRoleInProject("manager");
        manager.setStatus("confirmed");
        when(projectService.getMembers(1L)).thenReturn(List.of(manager));

        SysUser managerUser = new SysUser();
        managerUser.setId(10L);
        managerUser.setRealName("张主任");
        managerUser.setDept("规划一室");
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(managerUser));

        SysProjectStage stage = new SysProjectStage();
        stage.setId(100L);
        stage.setStageName("外业调查");
        stage.setDescription("开展样地调查和数据采集");
        stage.setAssigneeId(10L);
        stage.setStatus("in_progress");
        stage.setSortOrder(1);
        when(stageService.listByProjectId(1L)).thenReturn(new ArrayList<>(List.of(stage)));

        SysStageReport firstReport = new SysStageReport();
        firstReport.setStageId(100L);
        firstReport.setContent("第一次阶段填报");
        firstReport.setProblem("设备不足");
        firstReport.setQualityControl("抽样复核");
        firstReport.setSubmitUserId(10L);
        firstReport.setSubmitTime(LocalDateTime.of(2026, 6, 1, 9, 0));

        SysStageReport secondReport = new SysStageReport();
        secondReport.setStageId(100L);
        secondReport.setContent("第二次阶段填报");
        secondReport.setResultSummary("完成十个样地调查");
        secondReport.setSubmitUserId(10L);
        secondReport.setSubmitTime(LocalDateTime.of(2026, 6, 2, 9, 0));
        when(stageReportMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(secondReport, firstReport));

        SysDeviation deviation = new SysDeviation();
        deviation.setStageId(100L);
        deviation.setType("进度偏差");
        deviation.setDescription("外业延期");
        deviation.setReason("连续降雨");
        deviation.setImpact("延期三天");
        deviation.setStatus("open");
        when(deviationService.listByProject(1L)).thenReturn(List.of(deviation));

        SysSupportItem support = new SysSupportItem();
        support.setTitle("协调外业车辆");
        support.setContent("申请增加一辆外业车");
        support.setReply("已安排");
        support.setResolveNote("车辆已到位");
        support.setStatus("resolved");
        when(supportItemService.listByProject(1L)).thenReturn(List.of(support));

        when(changeService.listByProject(1L)).thenReturn(List.of());
        SysReview review = new SysReview();
        review.setSupportEvaluation("跨部门支持响应及时");
        when(reviewService.getByProjectId(1L)).thenReturn(review);
        when(experienceService.getByProjectId(1L)).thenReturn(null);
        when(approvalService.getByProject(1L)).thenReturn(null);

        byte[] bytes = exportService.exportProject(1L);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet overview = workbook.getSheet("项目概况");
            Sheet reports = workbook.getSheet("阶段与填报");
            Sheet controls = workbook.getSheet("偏差与支持");
            Sheet ledger = workbook.getSheet("项目台账");
            assertNotNull(overview);
            assertNotNull(reports);
            assertNotNull(controls);

            assertTrue(sheetContains(overview, "项目页面中的完整项目描述"));
            assertTrue(sheetContains(overview, "雨季外业进度风险"));
            assertTrue(sheetContains(overview, "张主任"));
            assertTrue(sheetContains(reports, "第一次阶段填报"));
            assertTrue(sheetContains(reports, "第二次阶段填报"));
            assertTrue(sheetContains(reports, "抽样复核"));
            assertTrue(sheetContains(controls, "连续降雨"));
            assertTrue(sheetContains(controls, "协调外业车辆"));
            assertTrue(sheetContains(controls, "车辆已到位"));
            assertTrue(sheetContains(ledger, "支持评价: 跨部门支持响应及时"));
        }
    }

    private boolean sheetContains(Sheet sheet, String expected) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (expected.equals(cell.toString())) {
                    return true;
                }
            }
        }
        return false;
    }
}
