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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

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
            supportItem.setTitle(hasText(supportTitle) ? supportTitle : content);
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
        return Result.ok(reportService.review(reportId, status, comment, loginUser.getUser().getId()));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
