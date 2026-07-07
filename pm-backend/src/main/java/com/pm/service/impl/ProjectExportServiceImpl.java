package com.pm.service.impl;

import com.pm.entity.*;
import com.pm.mapper.*;
import com.pm.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectExportServiceImpl implements ProjectExportService {

    private final SysProjectService projectService;
    private final SysProjectStageService stageService;
    private final SysStageReportMapper stageReportMapper;
    private final SysDeviationService deviationService;
    private final SysChangeService changeService;
    private final SysReviewService reviewService;
    private final SysExperienceService experienceService;
    private final SysApprovalService approvalService;
    private final SysSupportItemService supportItemService;
    private final SysUserMapper userMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public byte[] exportProject(Long projectId) {
        SysProject project = projectService.getById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        List<SysProjectStage> stages = stageService.listByProjectId(projectId);
        stages.sort(Comparator.comparing(s -> s.getSortOrder() == null ? 0 : s.getSortOrder()));

        List<SysDeviation> deviations = deviationService.listByProject(projectId);
        Map<Long, List<SysDeviation>> deviationsByStage = deviations.stream()
                .filter(d -> d.getStageId() != null)
                .collect(Collectors.groupingBy(SysDeviation::getStageId));

        List<SysStageReport> allReports = getAllReports(projectId);
        Map<Long, SysStageReport> latestReportByStage = allReports.stream()
                .filter(r -> r.getStageId() != null)
                .collect(Collectors.toMap(SysStageReport::getStageId, r -> r, (a, b) -> {
                    return a.getSubmitTime() != null && (b.getSubmitTime() == null || a.getSubmitTime().isAfter(b.getSubmitTime())) ? a : b;
                }));

        List<SysChange> changes = changeService.listByProject(projectId);
        List<SysProjectMember> members = projectService.getMembers(projectId);
        List<SysSupportItem> supportItems = supportItemService.listByProject(projectId);
        SysReview review = reviewService.getByProjectId(projectId);
        SysExperience experience = experienceService.getByProjectId(projectId);
        SysApproval approval = approvalService.getByProject(projectId);
        Map<Long, SysUser> usersById = loadRelatedUsers(stages, allReports, deviations, supportItems,
                changes, members, review, experience, approval);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("项目台账");

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle sectionTitleStyle = createSectionTitleStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle wrapStyle = createWrapDataStyle(workbook);

            int rowIdx = 0;

            // Row 1: Title
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeightInPoints(28);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("项目全过程台账");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            // Row 2: Project name
            Row nameRow = sheet.createRow(rowIdx++);
            nameRow.setHeightInPoints(22);
            Cell nameCell = nameRow.createCell(0);
            nameCell.setCellValue("项目名称: " + (project.getName() != null ? project.getName() : ""));
            nameCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

            // Row 3: empty
            rowIdx++;

            // Row 4: Headers
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(22);
            String[] headers = {"闸口", "阶段", "计划开始", "计划结束", "实际开始", "实际结束", "偏差情况", "填报内容/说明"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows: stages
            for (int i = 0; i < stages.size(); i++) {
                SysProjectStage stage = stages.get(i);
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(20);

                String gateName = getGateName(i, stages.size());

                createCell(row, 0, gateName, dataStyle);
                createCell(row, 1, stage.getStageName(), dataStyle);
                createDateCell(row, 2, stage.getPlanStart(), dateStyle, workbook);
                createDateCell(row, 3, stage.getPlanEnd(), dateStyle, workbook);
                createDateCell(row, 4, stage.getActualStart(), dateStyle, workbook);
                createDateCell(row, 5, stage.getActualEnd(), dateStyle, workbook);

                // Deviation info
                List<SysDeviation> stageDeviations = deviationsByStage.getOrDefault(stage.getId(), Collections.emptyList());
                String deviationText = stageDeviations.isEmpty() ? "" : stageDeviations.stream()
                        .map(d -> (d.getType() != null ? d.getType() : "") + ": " + (d.getDescription() != null ? d.getDescription() : ""))
                        .collect(Collectors.joining("; "));
                createCell(row, 6, deviationText, wrapStyle);

                // Latest report content
                SysStageReport latestReport = latestReportByStage.get(stage.getId());
                String reportContent = "";
                if (latestReport != null && latestReport.getContent() != null) {
                    reportContent = latestReport.getContent();
                }
                createCell(row, 7, reportContent, wrapStyle);
            }

            // Add separator row (empty)
            rowIdx++;

            // --- 变更记录 Section ---
            rowIdx = addSectionTitle(sheet, rowIdx, "变更记录", sectionTitleStyle);
            if (changes != null && !changes.isEmpty()) {
                for (SysChange change : changes) {
                    Row row = sheet.createRow(rowIdx++);
                    row.setHeightInPoints(20);
                    createCell(row, 0, "变更控制", dataStyle);
                    createCell(row, 1, "变更记录", dataStyle);
                    createDateCell(row, 2, change.getConfirmTime(), dateStyle, workbook);
                    createCell(row, 3, "", dataStyle);
                    createCell(row, 4, "", dataStyle);
                    createCell(row, 5, "", dataStyle);
                    String changeDesc = buildChangeDescription(change);
                    createCell(row, 6, changeDesc, wrapStyle);
                    createCell(row, 7, change.getContent(), wrapStyle);
                }
            } else {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, "变更控制", dataStyle);
                createCell(row, 1, "变更记录", dataStyle);
                createCell(row, 7, "暂无变更记录", dataStyle);
            }

            rowIdx++;

            // --- 自评 Section ---
            rowIdx = addSectionTitle(sheet, rowIdx, "自评", sectionTitleStyle);
            if (review != null) {
                String selfEval = buildSelfEvaluation(review);
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(20);
                createCell(row, 0, "收尾审批", dataStyle);
                createCell(row, 1, "项目自评", dataStyle);
                createCell(row, 2, "", dataStyle);
                createCell(row, 3, "", dataStyle);
                createCell(row, 4, "", dataStyle);
                createCell(row, 5, "", dataStyle);
                createCell(row, 6, "", dataStyle);
                createCell(row, 7, selfEval, wrapStyle);
            } else {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, "收尾审批", dataStyle);
                createCell(row, 1, "项目自评", dataStyle);
                createCell(row, 7, "暂无自评数据", dataStyle);
            }

            rowIdx++;

            // --- 经验总结 Section ---
            rowIdx = addSectionTitle(sheet, rowIdx, "经验总结", sectionTitleStyle);
            if (experience != null) {
                String expText = buildExperienceText(experience);
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(20);
                createCell(row, 0, "总结沉淀", dataStyle);
                createCell(row, 1, "经验总结", dataStyle);
                createCell(row, 2, "", dataStyle);
                createCell(row, 3, "", dataStyle);
                createCell(row, 4, "", dataStyle);
                createCell(row, 5, "", dataStyle);
                createCell(row, 6, "", dataStyle);
                createCell(row, 7, expText, wrapStyle);
            } else {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, "总结沉淀", dataStyle);
                createCell(row, 1, "经验总结", dataStyle);
                createCell(row, 7, "暂无经验总结数据", dataStyle);
            }

            rowIdx++;

            // --- 成果评审 Section ---
            rowIdx = addSectionTitle(sheet, rowIdx, "成果评审", sectionTitleStyle);
            if (approval != null) {
                String approvalText = buildApprovalText(approval);
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(20);
                createCell(row, 0, "收尾审批", dataStyle);
                createCell(row, 1, "成果评审", dataStyle);
                createDateCell(row, 2, approval.getConfirmTime(), dateStyle, workbook);
                createCell(row, 3, "", dataStyle);
                createCell(row, 4, "", dataStyle);
                createCell(row, 5, "", dataStyle);
                createCell(row, 6, "", dataStyle);
                createCell(row, 7, approvalText, wrapStyle);
            } else {
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, "收尾审批", dataStyle);
                createCell(row, 1, "成果评审", dataStyle);
                createCell(row, 7, "暂无成果评审数据", dataStyle);
            }

            createProjectOverviewSheet(workbook, project, members, usersById, titleStyle, headerStyle,
                    sectionTitleStyle, dataStyle, wrapStyle);
            createStageReportSheet(workbook, stages, allReports, usersById, titleStyle, headerStyle, dataStyle, wrapStyle);
            createControlSheet(workbook, stages, deviations, supportItems, usersById,
                    titleStyle, headerStyle, sectionTitleStyle, dataStyle, wrapStyle);

            // Auto-size columns (with max width constraint)
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                // Cap column width at reasonable values
                if (i == 0) {
                    sheet.setColumnWidth(i, Math.min(currentWidth, 16 * 256));
                } else if (i == 1) {
                    sheet.setColumnWidth(i, Math.min(currentWidth, 20 * 256));
                } else if (i == 6 || i == 7) {
                    sheet.setColumnWidth(i, Math.max(Math.min(currentWidth, 50 * 256), 30 * 256));
                } else {
                    sheet.setColumnWidth(i, Math.min(currentWidth, 18 * 256));
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    private List<SysStageReport> getAllReports(Long projectId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysStageReport> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("project_id", projectId).orderByDesc("submit_time");
        return stageReportMapper.selectList(wrapper);
    }

    private Map<Long, SysUser> loadRelatedUsers(List<SysProjectStage> stages,
                                                List<SysStageReport> reports,
                                                List<SysDeviation> deviations,
                                                List<SysSupportItem> supportItems,
                                                List<SysChange> changes,
                                                List<SysProjectMember> members,
                                                SysReview review,
                                                SysExperience experience,
                                                SysApproval approval) {
        Set<Long> userIds = new LinkedHashSet<>();
        stages.forEach(item -> addUserId(userIds, item.getAssigneeId()));
        reports.forEach(item -> {
            addUserId(userIds, item.getSubmitUserId());
            addUserId(userIds, item.getReviewUserId());
        });
        deviations.forEach(item -> addUserId(userIds, item.getCreateUserId()));
        supportItems.forEach(item -> {
            addUserId(userIds, item.getApplicantId());
            addUserId(userIds, item.getHandlerId());
        });
        changes.forEach(item -> addUserId(userIds, item.getCreateUserId()));
        members.forEach(item -> addUserId(userIds, item.getUserId()));
        if (review != null) addUserId(userIds, review.getCreateUserId());
        if (experience != null) addUserId(userIds, experience.getCreateUserId());
        if (approval != null) addUserId(userIds, approval.getCreateUserId());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysUser> users = userMapper.selectBatchIds(userIds);
        if (users == null) {
            return Collections.emptyMap();
        }
        return users.stream().collect(Collectors.toMap(SysUser::getId, user -> user, (a, b) -> a));
    }

    private void addUserId(Set<Long> userIds, Long userId) {
        if (userId != null) {
            userIds.add(userId);
        }
    }

    private void createProjectOverviewSheet(Workbook workbook,
                                            SysProject project,
                                            List<SysProjectMember> members,
                                            Map<Long, SysUser> usersById,
                                            CellStyle titleStyle,
                                            CellStyle headerStyle,
                                            CellStyle sectionStyle,
                                            CellStyle dataStyle,
                                            CellStyle wrapStyle) {
        Sheet sheet = workbook.createSheet("项目概况");
        int rowIdx = addWideTitle(sheet, 0, "项目概况与启动策划", 3, titleStyle);

        rowIdx = addWideSectionTitle(sheet, rowIdx, "基本信息", 3, sectionStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "项目名称", project.getName(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "项目状态", projectStatusText(project.getStatus()), headerStyle, dataStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "项目描述", project.getDescription(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "创建时间", formatDateTime(project.getCreateTime()), headerStyle, dataStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "更新时间", formatDateTime(project.getUpdateTime()), headerStyle, dataStyle);

        rowIdx = addWideSectionTitle(sheet, rowIdx, "客户与立项判断", 3, sectionStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "客户等级/项目分级", project.getCustomerLevel(), headerStyle, dataStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "双方联系人", project.getContacts(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "成果产出类型", project.getAchievementType(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "审核审批要求", project.getApprovalRequirements(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "项目重要性", project.getProjectImportance(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "成果方向及附件", project.getAchievementDirection(), headerStyle, wrapStyle);

        rowIdx = addWideSectionTitle(sheet, rowIdx, "前期分析与约束", 3, sectionStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "能否承接判断", project.getCanUndertake(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "主要风险", project.getMainRisks(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "关键约束", project.getKeyConstraints(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "成果交付要求", project.getDeliverableRequirements(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "预计审批路径", project.getApprovalPath(), headerStyle, wrapStyle);

        rowIdx = addWideSectionTitle(sheet, rowIdx, "策划与资源配置", 3, sectionStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "人力资源配置", project.getHrAllocation(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "预计阶段成果", project.getExpectedOutputs(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "核心资料", project.getCoreMaterials(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "项目组组建", project.getTeamSetup(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "核心策略", project.getCoreStrategy(), headerStyle, wrapStyle);

        rowIdx = addWideSectionTitle(sheet, rowIdx, "项目获取与审批", 3, sectionStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "投标情况", project.getBidSituation(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "采购程序", project.getProcurementInfo(), headerStyle, wrapStyle);
        rowIdx = addOverviewField(sheet, rowIdx, "获取结果", project.getAcquisitionResult(), headerStyle, wrapStyle);

        rowIdx = addWideSectionTitle(sheet, rowIdx, "项目成员", 3, sectionStyle);
        String[] memberHeaders = {"姓名", "部门", "项目角色", "确认状态"};
        createHeaderRow(sheet, rowIdx++, memberHeaders, headerStyle);
        if (members.isEmpty()) {
            Row row = sheet.createRow(rowIdx);
            createCell(row, 0, "暂无项目成员", dataStyle);
        } else {
            for (SysProjectMember member : members) {
                SysUser user = usersById.get(member.getUserId());
                Row row = sheet.createRow(rowIdx++);
                createCell(row, 0, user != null ? user.getRealName() : "", dataStyle);
                createCell(row, 1, user != null ? user.getDept() : "", dataStyle);
                createCell(row, 2, projectRoleText(member.getRoleInProject()), dataStyle);
                createCell(row, 3, memberStatusText(member.getStatus()), dataStyle);
            }
        }

        sheet.setColumnWidth(0, 22 * 256);
        sheet.setColumnWidth(1, 34 * 256);
        sheet.setColumnWidth(2, 22 * 256);
        sheet.setColumnWidth(3, 22 * 256);
        sheet.createFreezePane(0, 1);
    }

    private void createStageReportSheet(Workbook workbook,
                                        List<SysProjectStage> stages,
                                        List<SysStageReport> reports,
                                        Map<Long, SysUser> usersById,
                                        CellStyle titleStyle,
                                        CellStyle headerStyle,
                                        CellStyle dataStyle,
                                        CellStyle wrapStyle) {
        Sheet sheet = workbook.createSheet("阶段与填报");
        String[] headers = {
                "阶段名称", "阶段描述", "责任人", "阶段状态", "计划开始", "计划结束", "实际开始", "实际结束",
                "填报时间", "填报人", "进度", "填报内容", "存在问题", "质量控制", "成果说明", "协调事项",
                "部门审核", "审核状态", "审核意见", "附件名称"
        };
        int rowIdx = addWideTitle(sheet, 0, "项目阶段与全部填报记录", headers.length - 1, titleStyle);
        createHeaderRow(sheet, rowIdx++, headers, headerStyle);

        Map<Long, List<SysStageReport>> reportsByStage = reports.stream()
                .filter(report -> report.getStageId() != null)
                .collect(Collectors.groupingBy(SysStageReport::getStageId));

        for (SysProjectStage stage : stages) {
            List<SysStageReport> stageReports = new ArrayList<>(
                    reportsByStage.getOrDefault(stage.getId(), Collections.emptyList()));
            stageReports.sort(Comparator.comparing(SysStageReport::getSubmitTime,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            if (stageReports.isEmpty()) {
                rowIdx = addStageReportRow(sheet, rowIdx, stage, null, usersById, dataStyle, wrapStyle);
            } else {
                for (SysStageReport report : stageReports) {
                    rowIdx = addStageReportRow(sheet, rowIdx, stage, report, usersById, dataStyle, wrapStyle);
                }
            }
        }

        if (stages.isEmpty()) {
            Row row = sheet.createRow(rowIdx);
            createCell(row, 0, "暂无项目阶段", dataStyle);
        }
        setDetailSheetWidths(sheet, headers.length, Set.of(1, 11, 12, 13, 14, 15, 16, 18));
        sheet.createFreezePane(0, 2);
        sheet.setAutoFilter(new CellRangeAddress(1, Math.max(1, rowIdx - 1), 0, headers.length - 1));
    }

    private int addStageReportRow(Sheet sheet,
                                  int rowIdx,
                                  SysProjectStage stage,
                                  SysStageReport report,
                                  Map<Long, SysUser> usersById,
                                  CellStyle dataStyle,
                                  CellStyle wrapStyle) {
        Row row = sheet.createRow(rowIdx++);
        row.setHeightInPoints(34);
        createCell(row, 0, stage.getStageName(), dataStyle);
        createCell(row, 1, stage.getDescription(), wrapStyle);
        createCell(row, 2, userName(usersById, stage.getAssigneeId()), dataStyle);
        createCell(row, 3, stageStatusText(stage.getStatus()), dataStyle);
        createCell(row, 4, formatDate(stage.getPlanStart()), dataStyle);
        createCell(row, 5, formatDate(stage.getPlanEnd()), dataStyle);
        createCell(row, 6, formatDate(stage.getActualStart()), dataStyle);
        createCell(row, 7, formatDate(stage.getActualEnd()), dataStyle);
        createCell(row, 8, report != null ? formatDateTime(report.getSubmitTime()) : "", dataStyle);
        createCell(row, 9, report != null ? userName(usersById, report.getSubmitUserId()) : "", dataStyle);
        createCell(row, 10, report != null && report.getProgressRate() != null ? report.getProgressRate() + "%" : "", dataStyle);
        createCell(row, 11, report != null ? report.getContent() : "", wrapStyle);
        createCell(row, 12, report != null ? report.getProblem() : "", wrapStyle);
        createCell(row, 13, report != null ? report.getQualityControl() : "", wrapStyle);
        createCell(row, 14, report != null ? report.getResultSummary() : "", wrapStyle);
        createCell(row, 15, report != null ? report.getCoordinationNote() : "", wrapStyle);
        createCell(row, 16, report != null ? report.getDeptReviewNote() : "", wrapStyle);
        createCell(row, 17, report != null ? reportStatusText(report.getReviewStatus()) : "", dataStyle);
        createCell(row, 18, report != null ? report.getReviewComment() : "", wrapStyle);
        createCell(row, 19, report != null ? report.getAttachmentName() : "", dataStyle);
        return rowIdx;
    }

    private void createControlSheet(Workbook workbook,
                                    List<SysProjectStage> stages,
                                    List<SysDeviation> deviations,
                                    List<SysSupportItem> supportItems,
                                    Map<Long, SysUser> usersById,
                                    CellStyle titleStyle,
                                    CellStyle headerStyle,
                                    CellStyle sectionStyle,
                                    CellStyle dataStyle,
                                    CellStyle wrapStyle) {
        Sheet sheet = workbook.createSheet("偏差与支持");
        int lastColumn = 9;
        int rowIdx = addWideTitle(sheet, 0, "项目偏差与支持事项", lastColumn, titleStyle);
        Map<Long, String> stageNames = stages.stream()
                .filter(stage -> stage.getId() != null)
                .collect(Collectors.toMap(SysProjectStage::getId, SysProjectStage::getStageName, (a, b) -> a));

        rowIdx = addWideSectionTitle(sheet, rowIdx, "偏差台账", lastColumn, sectionStyle);
        String[] deviationHeaders = {"阶段", "偏差类型", "偏差描述", "原因", "影响", "状态", "创建人", "创建时间", "关闭时间"};
        createHeaderRow(sheet, rowIdx++, deviationHeaders, headerStyle);
        if (deviations.isEmpty()) {
            Row row = sheet.createRow(rowIdx++);
            createCell(row, 0, "暂无偏差记录", dataStyle);
        } else {
            for (SysDeviation deviation : deviations) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(34);
                createCell(row, 0, stageNames.getOrDefault(deviation.getStageId(), "未关联阶段"), dataStyle);
                createCell(row, 1, deviation.getType(), dataStyle);
                createCell(row, 2, deviation.getDescription(), wrapStyle);
                createCell(row, 3, deviation.getReason(), wrapStyle);
                createCell(row, 4, deviation.getImpact(), wrapStyle);
                createCell(row, 5, deviationStatusText(deviation.getStatus()), dataStyle);
                createCell(row, 6, userName(usersById, deviation.getCreateUserId()), dataStyle);
                createCell(row, 7, formatDateTime(deviation.getCreateTime()), dataStyle);
                createCell(row, 8, formatDateTime(deviation.getCloseTime()), dataStyle);
            }
        }

        rowIdx++;
        rowIdx = addWideSectionTitle(sheet, rowIdx, "支持事项", lastColumn, sectionStyle);
        String[] supportHeaders = {"标题", "内容", "申请人", "处理人", "期望时间", "状态", "处理回复", "解决情况", "创建时间", "更新时间"};
        createHeaderRow(sheet, rowIdx++, supportHeaders, headerStyle);
        if (supportItems.isEmpty()) {
            Row row = sheet.createRow(rowIdx);
            createCell(row, 0, "暂无支持事项", dataStyle);
        } else {
            for (SysSupportItem item : supportItems) {
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(34);
                createCell(row, 0, item.getTitle(), dataStyle);
                createCell(row, 1, item.getContent(), wrapStyle);
                createCell(row, 2, userName(usersById, item.getApplicantId()), dataStyle);
                createCell(row, 3, userName(usersById, item.getHandlerId()), dataStyle);
                createCell(row, 4, formatDate(item.getExpectTime()), dataStyle);
                createCell(row, 5, supportStatusText(item.getStatus()), dataStyle);
                createCell(row, 6, item.getReply(), wrapStyle);
                createCell(row, 7, item.getResolveNote(), wrapStyle);
                createCell(row, 8, formatDateTime(item.getCreateTime()), dataStyle);
                createCell(row, 9, formatDateTime(item.getUpdateTime()), dataStyle);
            }
        }

        setDetailSheetWidths(sheet, lastColumn + 1, Set.of(1, 2, 3, 4, 6, 7));
        sheet.createFreezePane(0, 2);
    }

    private int addWideTitle(Sheet sheet, int rowIdx, String title, int lastColumn, CellStyle style) {
        Row row = sheet.createRow(rowIdx++);
        row.setHeightInPoints(28);
        createCell(row, 0, title, style);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, lastColumn));
        return rowIdx;
    }

    private int addWideSectionTitle(Sheet sheet, int rowIdx, String title, int lastColumn, CellStyle style) {
        Row row = sheet.createRow(rowIdx++);
        row.setHeightInPoints(22);
        createCell(row, 0, title, style);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, lastColumn));
        return rowIdx;
    }

    private int addOverviewField(Sheet sheet,
                                 int rowIdx,
                                 String label,
                                 String value,
                                 CellStyle labelStyle,
                                 CellStyle valueStyle) {
        Row row = sheet.createRow(rowIdx++);
        row.setHeightInPoints(value != null && (value.contains("\n") || value.length() > 60) ? 42 : 24);
        createCell(row, 0, label, labelStyle);
        createCell(row, 1, value, valueStyle);
        createCell(row, 2, "", valueStyle);
        createCell(row, 3, "", valueStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 1, 3));
        return rowIdx;
    }

    private void createHeaderRow(Sheet sheet, int rowIdx, String[] headers, CellStyle headerStyle) {
        Row row = sheet.createRow(rowIdx);
        row.setHeightInPoints(24);
        for (int i = 0; i < headers.length; i++) {
            createCell(row, i, headers[i], headerStyle);
        }
    }

    private void setDetailSheetWidths(Sheet sheet, int columnCount, Set<Integer> wideColumns) {
        for (int i = 0; i < columnCount; i++) {
            sheet.setColumnWidth(i, (wideColumns.contains(i) ? 30 : 16) * 256);
        }
    }

    private String userName(Map<Long, SysUser> usersById, Long userId) {
        if (userId == null) return "";
        SysUser user = usersById.get(userId);
        return user != null && user.getRealName() != null ? user.getRealName() : "";
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "";
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FMT) : "";
    }

    private String projectStatusText(String status) {
        return "completed".equals(status) ? "已完成" : "active".equals(status) ? "进行中" : safeText(status);
    }

    private String projectRoleText(String role) {
        return "manager".equals(role) ? "项目负责人" : "engineer".equals(role) ? "工程师" : safeText(role);
    }

    private String memberStatusText(String status) {
        return "confirmed".equals(status) ? "已确认" : "pending".equals(status) ? "待确认" : safeText(status);
    }

    private String stageStatusText(String status) {
        if ("pending".equals(status)) return "待开始";
        if ("in_progress".equals(status)) return "进行中";
        if ("submitted".equals(status)) return "待审阅";
        if ("completed".equals(status)) return "已完成";
        return safeText(status);
    }

    private String reportStatusText(String status) {
        if ("pending".equals(status)) return "待审阅";
        if ("passed".equals(status)) return "已通过";
        if ("rejected".equals(status)) return "已退回";
        return safeText(status);
    }

    private String deviationStatusText(String status) {
        return "open".equals(status) ? "未关闭" : "closed".equals(status) ? "已关闭" : safeText(status);
    }

    private String supportStatusText(String status) {
        if ("pending".equals(status)) return "待处理";
        if ("processing".equals(status)) return "处理中";
        if ("resolved".equals(status)) return "已解决";
        if ("closed".equals(status)) return "已关闭";
        return safeText(status);
    }

    private String safeText(String value) {
        return value != null ? value : "";
    }

    private String getGateName(int index, int total) {
        if (total == 1) {
            return "启动与策划";
        }
        if (index == 0) {
            return "启动与策划";
        }
        if (index == total - 1) {
            return "总结沉淀";
        }
        // Distribute remaining gates proportionally
        int middleCount = total - 2;
        if (middleCount <= 0) {
            return "执行与监控";
        }
        int middleIndex = index - 1;
        if (middleCount == 1) {
            return "执行与监控";
        }
        if (middleCount == 2) {
            return middleIndex == 0 ? "执行与监控" : "收尾审批";
        }
        // 3+ middle stages: distribute among 执行与监控, 变更控制, 收尾审批
        if (middleCount == 3) {
            String[] gates = {"执行与监控", "变更控制", "收尾审批"};
            return gates[middleIndex];
        }
        // More stages: first third 执行与监控, last third 收尾审批, middle third 变更控制
        double perGate = middleCount / 3.0;
        if (middleIndex < perGate) {
            return "执行与监控";
        } else if (middleIndex < 2 * perGate) {
            return "变更控制";
        } else {
            return "收尾审批";
        }
    }

    private int addSectionTitle(Sheet sheet, int rowIdx, String title, CellStyle style) {
        Row row = sheet.createRow(rowIdx++);
        row.setHeightInPoints(22);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 7));
        return rowIdx;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createDateCell(Row row, int col, LocalDate date, CellStyle style, Workbook workbook) {
        Cell cell = row.createCell(col);
        if (date != null) {
            cell.setCellValue(date.format(DATE_FMT));
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    private String buildChangeDescription(SysChange change) {
        StringBuilder sb = new StringBuilder();
        if (change.getStatus() != null) {
            sb.append("状态: ").append("confirmed".equals(change.getStatus()) ? "已确认" : "待确认");
        }
        if (change.getImpact() != null && !change.getImpact().isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("影响: ").append(change.getImpact());
        }
        return sb.toString();
    }

    private String buildSelfEvaluation(SysReview review) {
        StringBuilder sb = new StringBuilder();
        if (review.getEfficiencyRating() != null && !review.getEfficiencyRating().isEmpty()) {
            sb.append("效率评价: ").append(review.getEfficiencyRating());
        }
        if (review.getQualityRating() != null && !review.getQualityRating().isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("质量评价: ").append(review.getQualityRating());
        }
        if (review.getOverallDeviation() != null && !review.getOverallDeviation().isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("总体偏差: ").append(review.getOverallDeviation());
        }
        if (review.getSupportEvaluation() != null && !review.getSupportEvaluation().isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("支持评价: ").append(review.getSupportEvaluation());
        }
        if (review.getCommunicationNote() != null && !review.getCommunicationNote().isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("沟通备注: ").append(review.getCommunicationNote());
        }
        return sb.toString();
    }

    private String buildExperienceText(SysExperience exp) {
        StringBuilder sb = new StringBuilder();
        if (exp.getReusableExperience() != null && !exp.getReusableExperience().isEmpty()) {
            sb.append("可复用经验: ").append(exp.getReusableExperience());
        }
        if (exp.getShortcomings() != null && !exp.getShortcomings().isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("不足: ").append(exp.getShortcomings());
        }
        if (exp.getRisks() != null && !exp.getRisks().isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("风险: ").append(exp.getRisks());
        }
        if (exp.getImprovement() != null && !exp.getImprovement().isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("改进建议: ").append(exp.getImprovement());
        }
        return sb.toString();
    }

    private String buildApprovalText(SysApproval approval) {
        StringBuilder sb = new StringBuilder();
        if (approval.getReviewSituation() != null && !approval.getReviewSituation().isEmpty()) {
            sb.append("评审情况: ").append(approval.getReviewSituation());
        }
        if (approval.getFailReason() != null && !approval.getFailReason().isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("未通过原因: ").append(approval.getFailReason());
        }
        return sb.toString();
    }

    // ---- Cell Styles ----

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderThin(style);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorderThin(style);
        return style;
    }

    private CellStyle createSectionTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorderThin(style);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderThin(style);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderThin(style);
        return style;
    }

    private CellStyle createWrapDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setBorderThin(style);
        return style;
    }

    private void setBorderThin(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
