package com.pm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.common.Result;
import com.pm.entity.SysDeviation;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.entity.SysSupportItem;
import com.pm.entity.SysUser;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysProjectStageMapper;
import com.pm.mapper.SysStageReportMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.security.LoginUser;
import com.pm.service.ProjectAccessService;
import com.pm.service.CacheEvictionService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final SysStageReportMapper reportMapper;
    private final SysProjectStageMapper stageMapper;
    private final SysProjectMapper projectMapper;
    private final SysProjectMemberMapper memberMapper;
    private final SysUserMapper userMapper;
    private final ProjectAccessService accessService;
    private final CacheEvictionService cacheEvictionService;

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
        SysProjectStage stage = stageMapper.selectById(stageId);
        if (stage != null) {
            accessService.requireProjectActive(stage.getProjectId());
        }
        if (progressRate == null || progressRate < 0 || progressRate > 100) {
            return Result.fail(400, "进度必须在0-100之间");
        }
        // 校验进度不能倒退：新填报的进度必须不低于该阶段历史最高进度
        List<SysStageReport> history = reportMapper.selectList(new LambdaQueryWrapper<SysStageReport>()
                .eq(SysStageReport::getStageId, stageId)
                .orderByDesc(SysStageReport::getSubmitTime));
        if (!history.isEmpty()) {
            int maxRate = history.stream().mapToInt(r -> r.getProgressRate() != null ? r.getProgressRate() : 0).max().orElse(0);
            if (progressRate < maxRate) {
                return Result.fail(400, "进度不能低于历史最高值（" + maxRate + "%），当前填报为 " + progressRate + "%");
            }
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

        cacheEvictionService.evictDashboardCaches();
        return Result.ok(saved);
    }

    @GetMapping("/reports/pending")
    public Result<Page<SysStageReport>> pendingReview(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (size > 100) size = 100;
        boolean isAdminOrLeader = "admin".equals(loginUser.getUser().getRole())
                || "leader".equals(loginUser.getUser().getRole());

        Page<SysStageReport> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<SysStageReport> wrapper = new LambdaQueryWrapper<SysStageReport>()
                .eq(SysStageReport::getReviewStatus, "pending")
                .orderByDesc(SysStageReport::getSubmitTime);

        if (!isAdminOrLeader) {
            List<SysProjectMember> memberships = memberMapper.selectList(
                    new LambdaQueryWrapper<SysProjectMember>()
                            .eq(SysProjectMember::getUserId, loginUser.getUser().getId())
                            .eq(SysProjectMember::getRoleInProject, "manager")
                            .eq(SysProjectMember::getStatus, "confirmed"));
            List<Long> managedProjectIds = memberships.stream()
                    .map(SysProjectMember::getProjectId).distinct().collect(Collectors.toList());
            if (managedProjectIds.isEmpty()) {
                return Result.ok(new Page<>(page, size, 0));
            }
            wrapper.in(SysStageReport::getProjectId, managedProjectIds);
        }

        pageObj = reportMapper.selectPage(pageObj, wrapper);
        List<SysStageReport> records = pageObj.getRecords();
        records.forEach(r -> r.setAttachmentData(null));

        // Batch enrichment to avoid N+1 queries
        Set<Long> stageIds = records.stream().map(SysStageReport::getStageId)
                .filter(id -> id != null).collect(Collectors.toSet());
        Set<Long> projectIds = records.stream().map(SysStageReport::getProjectId)
                .filter(id -> id != null).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(SysStageReport::getSubmitUserId)
                .filter(id -> id != null).collect(Collectors.toSet());

        Map<Long, String> stageNameMap = new HashMap<>();
        if (!stageIds.isEmpty()) {
            stageMapper.selectBatchIds(stageIds)
                    .forEach(s -> stageNameMap.put(s.getId(), s.getStageName()));
        }
        Map<Long, String> projectNameMap = new HashMap<>();
        if (!projectIds.isEmpty()) {
            projectMapper.selectBatchIds(projectIds)
                    .forEach(p -> projectNameMap.put(p.getId(), p.getName()));
        }
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds)
                    .forEach(u -> userNameMap.put(u.getId(), u.getRealName()));
        }

        for (SysStageReport r : records) {
            r.setStageName(stageNameMap.getOrDefault(r.getStageId(), "(阶段已删除)"));
            r.setProjectName(projectNameMap.getOrDefault(r.getProjectId(), "(项目已删除)"));
            r.setSubmitUserName(userNameMap.get(r.getSubmitUserId()));
        }

        return Result.ok(pageObj);
    }

    @GetMapping("/reports/{reportId}/attachment")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long reportId,
                                                     @AuthenticationPrincipal LoginUser loginUser) {
        SysStageReport report = reportService.getById(reportId);
        if (report == null || report.getAttachmentName() == null || report.getAttachmentData() == null) {
            return ResponseEntity.notFound().build();
        }
        accessService.requireReportView(reportId, loginUser.getUser());
        String filename = report.getAttachmentName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(report.getAttachmentData());
    }

    @PostMapping("/reports/{reportId}/review")
    public Result<SysStageReport> review(@PathVariable Long reportId,
                                         @RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal LoginUser loginUser) {
        SysStageReport report = reportService.getById(reportId);
        if (report == null) {
            return Result.fail(404, "填报记录不存在");
        }
        accessService.requireReportReview(reportId, loginUser.getUser());
        String status = body.get("reviewStatus");
        String comment = body.get("reviewComment");
        if (!"passed".equals(status) && !"returned".equals(status)) {
            throw new IllegalArgumentException("审核状态只能是通过或退回");
        }
        if ("returned".equals(status) && !hasText(comment)) {
            throw new IllegalArgumentException("退回填报时请填写审核意见");
        }
        SysStageReport reviewed = reportService.review(reportId, status, comment, loginUser.getUser().getId());
        cacheEvictionService.evictDashboardCaches();
        return Result.ok(reviewed);
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
