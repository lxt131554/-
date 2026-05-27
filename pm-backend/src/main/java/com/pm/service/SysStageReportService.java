package com.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pm.entity.SysStageReport;
import java.util.List;

public interface SysStageReportService extends IService<SysStageReport> {
    SysStageReport submit(Long stageId, SysStageReport report, Long userId);
    List<SysStageReport> listByStageId(Long stageId);
    SysStageReport review(Long reportId, String reviewStatus, String reviewComment, Long reviewerId);
    List<SysStageReport> listPendingReview(Long reviewerId);
    byte[] downloadAttachment(Long reportId);
}
