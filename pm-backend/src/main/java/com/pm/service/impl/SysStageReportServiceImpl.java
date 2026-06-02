package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.mapper.SysProjectStageMapper;
import com.pm.mapper.SysStageReportMapper;
import com.pm.service.SysStageReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysStageReportServiceImpl extends ServiceImpl<SysStageReportMapper, SysStageReport>
        implements SysStageReportService {

    private final SysProjectStageMapper stageMapper;
    private final com.pm.mapper.SysProjectMapper projectMapper;
    private final com.pm.mapper.SysUserMapper userMapper;

    @Override
    @Transactional
    public SysStageReport submit(Long stageId, SysStageReport report, Long userId) {
        SysProjectStage stage = stageMapper.selectById(stageId);
        if (stage == null) {
            throw new RuntimeException("阶段不存在");
        }
        report.setStageId(stageId);
        report.setProjectId(stage.getProjectId());
        report.setSubmitUserId(userId);
        report.setSubmitTime(LocalDateTime.now());
        report.setReviewStatus("pending");
        baseMapper.insert(report);

        stage.setStatus("submitted");
        stage.setActualStart(report.getActualStart());
        stage.setActualEnd(report.getActualEnd());
        stageMapper.updateById(stage);
        return report;
    }

    @Override
    public List<SysStageReport> listByStageId(Long stageId) {
        return baseMapper.selectList(
            new LambdaQueryWrapper<SysStageReport>()
                .eq(SysStageReport::getStageId, stageId)
                .orderByDesc(SysStageReport::getCreateTime)
        );
    }

    @Override
    @Transactional
    public SysStageReport review(Long reportId, String reviewStatus, String reviewComment, Long reviewerId) {
        SysStageReport report = baseMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("填报记录不存在");
        }
        report.setReviewStatus(reviewStatus);
        report.setReviewComment(reviewComment);
        report.setReviewUserId(reviewerId);
        report.setReviewTime(LocalDateTime.now());
        baseMapper.updateById(report);

        SysProjectStage stage = stageMapper.selectById(report.getStageId());
        if (stage != null) {
            if ("passed".equals(reviewStatus)) {
                stage.setStatus("completed");
            } else {
                stage.setStatus("in_progress");
            }
            stageMapper.updateById(stage);
        }
        return report;
    }

    @Override
    public List<SysStageReport> listPendingReview(Long reviewerId) {
        var reports = baseMapper.selectList(
            new LambdaQueryWrapper<SysStageReport>()
                .eq(SysStageReport::getReviewStatus, "pending")
                .orderByDesc(SysStageReport::getSubmitTime)
        );
        for (var r : reports) {
            var stage = stageMapper.selectById(r.getStageId());
            if (stage != null) r.setStageName(stage.getStageName());
            var project = projectMapper.selectById(r.getProjectId());
            if (project != null) r.setProjectName(project.getName());
            var user = userMapper.selectById(r.getSubmitUserId());
            if (user != null) r.setSubmitUserName(user.getRealName());
        }
        return reports;
    }

    @Override
    public byte[] downloadAttachment(Long reportId) {
        SysStageReport report = baseMapper.selectById(reportId);
        if (report == null || report.getAttachmentName() == null) {
            throw new RuntimeException("附件不存在");
        }
        return report.getAttachmentData();
    }
}
