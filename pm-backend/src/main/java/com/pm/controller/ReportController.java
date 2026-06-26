package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysDeviation;
import com.pm.entity.SysStageReport;
import com.pm.entity.SysSupportItem;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysDeviationService;
import com.pm.service.SysStageReportService;
import com.pm.service.SysSupportItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private static final long MAX_ATTACHMENT_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_ATTACHMENT_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".jpg", ".jpeg", ".png", ".zip", ".rar"
    );

    private final SysStageReportService reportService;
    private final SysDeviationService deviationService;
    private final SysSupportItemService supportItemService;
    private final ProjectAccessService accessService;

    @GetMapping("/stages/{stageId}/reports")
    public Result<List<SysStageReport>> listReports(@PathVariable Long stageId,
                                                    @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireStageView(stageId, loginUser.getUser());
        List<SysStageReport> list = reportService.listByStageId(stageId);
        list.forEach(r -> r.setAttachmentData(null));
        return Result.ok(list);
    }

    @PostMapping("/stages/{stageId}/reports")
    @Transactional
    public Result<SysStageReport> submit(
            @PathVariable Long stageId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "progressRate", defaultValue = "0") Integer progressRate,
            @RequestParam(value = "problem", required = false) String problem,
            @RequestParam(value = "qualityControl", required = false) String qualityControl,
            @RequestParam(value = "resultSummary", required = false) String resultSummary,
            @RequestParam(value = "coordinationNote", required = false) String coordinationNote,
            @RequestParam(value = "deptReviewNote", required = false) String deptReviewNote,
            @RequestParam(value = "actualStart", required = false) String actualStart,
            @RequestParam(value = "actualEnd", required = false) String actualEnd,
            @RequestParam(value = "isDeviation", defaultValue = "false") boolean isDeviation,
            @RequestParam(value = "deviationReason", required = false) String deviationReason,
            @RequestParam(value = "deviationImpact", required = false) String deviationImpact,
            @RequestParam(value = "needSupport", defaultValue = "false") boolean needSupport,
            @RequestParam(value = "supportTitle", required = false) String supportTitle,
            @RequestParam(value = "supportContent", required = false) String supportContent,
            @RequestParam(value = "supportHandlerId", required = false) Long supportHandlerId,
            @RequestParam(value = "supportExpectTime", required = false) String supportExpectTime,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal LoginUser loginUser) throws IOException {

        accessService.requireStageReport(stageId, loginUser.getUser());
        if (progressRate == null || progressRate < 0 || progressRate > 100) {
            throw new IllegalArgumentException("进度必须在 0-100 之间");
        }
        if (!hasText(content) && !hasText(problem) && !hasText(resultSummary) && !hasText(qualityControl)) {
            throw new IllegalArgumentException("请至少填写填报内容、问题、成果摘要或质量控制信息");
        }
        if (isDeviation && !hasText(problem) && !hasText(content)) {
            throw new IllegalArgumentException("标记偏差时请填写问题或填报内容");
        }
        if (needSupport && !hasText(supportTitle) && !hasText(supportContent)) {
            throw new IllegalArgumentException("需要支持时请填写支持标题或内容");
        }
        SysStageReport report = new SysStageReport();
        report.setContent(content);
        report.setProgressRate(progressRate);
        report.setProblem(problem);
        report.setQualityControl(qualityControl);
        report.setResultSummary(resultSummary);
        report.setCoordinationNote(coordinationNote);
        report.setDeptReviewNote(deptReviewNote);
        if (hasText(actualStart)) {
            report.setActualStart(LocalDate.parse(actualStart));
        }
        if (hasText(actualEnd)) {
            report.setActualEnd(LocalDate.parse(actualEnd));
        }

        if (file != null && !file.isEmpty()) {
            validateAttachment(file);
            report.setAttachmentName(file.getOriginalFilename());
            report.setAttachmentData(file.getBytes());
        }

        SysStageReport saved = reportService.submit(stageId, report, loginUser.getUser().getId());

        if (isDeviation) {
            String description = hasText(problem) ? problem : content;
            if (hasText(description)) {
                SysDeviation deviation = new SysDeviation();
                deviation.setProjectId(saved.getProjectId());
                deviation.setStageId(saved.getStageId());
                deviation.setReportId(saved.getId());
                deviation.setType("auto");
                deviation.setDescription(description);
                deviation.setReason(deviationReason);
                deviation.setImpact(deviationImpact);
                deviation.setStatus("open");
                deviation.setCreateUserId(loginUser.getUser().getId());
                deviationService.save(deviation);
            }
        }

        if (needSupport && (hasText(supportTitle) || hasText(supportContent))) {
            SysSupportItem supportItem = new SysSupportItem();
            supportItem.setProjectId(saved.getProjectId());
            supportItem.setTitle(hasText(supportTitle) ? supportTitle : "阶段支持申请");
            supportItem.setContent(hasText(supportContent) ? supportContent : problem);
            supportItem.setApplicantId(loginUser.getUser().getId());
            supportItem.setHandlerId(supportHandlerId);
            supportItem.setStatus("pending");
            if (hasText(supportExpectTime)) {
                supportItem.setExpectTime(LocalDate.parse(supportExpectTime));
            }
            supportItemService.save(supportItem);
        }

        return Result.ok(saved);
    }

    @GetMapping("/reports/pending")
    public Result<List<SysStageReport>> pendingReview(@AuthenticationPrincipal LoginUser loginUser) {
        List<SysStageReport> list = reportService.listPendingReview(
                loginUser.getUser().getId(),
                loginUser.getUser().getRole());
        list.forEach(r -> r.setAttachmentData(null));
        return Result.ok(list);
    }

    @GetMapping("/reports/{reportId}/attachment")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long reportId,
                                                     @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireReportView(reportId, loginUser.getUser());
        byte[] data = reportService.downloadAttachment(reportId);
        SysStageReport report = reportService.getById(reportId);
        String filename = report.getAttachmentName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @PostMapping("/reports/{reportId}/review")
    public Result<SysStageReport> review(@PathVariable Long reportId,
                                         @RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal LoginUser loginUser) {
        accessService.requireReportReview(reportId, loginUser.getUser());
        String status = body.get("reviewStatus");
        String comment = body.get("reviewComment");
        if (!"passed".equals(status) && !"returned".equals(status)) {
            throw new IllegalArgumentException("审核状态只能是通过或退回");
        }
        if ("returned".equals(status) && !hasText(comment)) {
            throw new IllegalArgumentException("退回填报时请填写审核意见");
        }
        return Result.ok(reportService.review(reportId, status, comment, loginUser.getUser().getId()));
    }

    private void validateAttachment(MultipartFile file) {
        if (file.getSize() > MAX_ATTACHMENT_BYTES) {
            throw new IllegalArgumentException("附件不能超过 10MB");
        }
        String filename = file.getOriginalFilename();
        if (!hasText(filename)) {
            throw new IllegalArgumentException("附件文件名不能为空");
        }
        String lowerName = filename.toLowerCase();
        boolean allowed = ALLOWED_ATTACHMENT_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
        if (!allowed) {
            throw new IllegalArgumentException("附件格式仅支持 pdf、doc、docx、xls、xlsx、jpg、png、zip、rar");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
