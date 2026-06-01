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

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

        List<SysStageReport> allReports = getLatestReports(projectId);
        Map<Long, SysStageReport> latestReportByStage = allReports.stream()
                .filter(r -> r.getStageId() != null)
                .collect(Collectors.toMap(SysStageReport::getStageId, r -> r, (a, b) -> {
                    return a.getSubmitTime() != null && (b.getSubmitTime() == null || a.getSubmitTime().isAfter(b.getSubmitTime())) ? a : b;
                }));

        List<SysChange> changes = changeService.listByProject(projectId);
        SysReview review = reviewService.getByProjectId(projectId);
        SysExperience experience = experienceService.getByProjectId(projectId);
        SysApproval approval = approvalService.getByProject(projectId);

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

    private List<SysStageReport> getLatestReports(Long projectId) {
        // Use MyBatis-Plus query wrapper to get reports for this project
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysStageReport> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("project_id", projectId).orderByDesc("submit_time");
        return stageReportMapper.selectList(wrapper);
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
