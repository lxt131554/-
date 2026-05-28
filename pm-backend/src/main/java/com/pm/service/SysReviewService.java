package com.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pm.entity.SysReview;

public interface SysReviewService extends IService<SysReview> {
    SysReview getByProjectId(Long projectId);
    SysReview saveOrUpdateReview(SysReview review, Long userId);
}
