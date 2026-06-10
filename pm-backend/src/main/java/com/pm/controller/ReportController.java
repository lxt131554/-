package com.pm.controller;

import com.pm.common.Result;
import com.pm.entity.SysStageReport;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.SysStageReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
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
        if (actualStart != null && !actualStart.isEmpty()) report.setActualStart(LocalDate.parse(actualStart));
        if (actualEnd != null && !actualEnd.isEmpty()) report.setActualEnd(LocalDate.parse(actualEnd));

        if (file != null && !file.isEmpty()) {
            report.setAttachmentName(file.getOriginalFilename());
            report.setAttachmentData(file.getBytes());
        }

        return Result.ok(reportService.submit(stageId, report, loginUser.getUser().getId()));
    }

    @GetMapping("/reports/pending")
    public Result<List<SysStageReport>> pendingReview(@AuthenticationPrincipal LoginUser loginUser) {
        List<SysStageReport> list = reportService.listPendingReview(loginUser.getUser().getId(), loginUser.getUser().getRole());
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
}
