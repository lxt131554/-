package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysReview;
import com.pm.mapper.SysReviewMapper;
import com.pm.service.SysReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysReviewServiceImpl extends ServiceImpl<SysReviewMapper, SysReview>
        implements SysReviewService {

    @Override
    public SysReview getByProjectId(Long projectId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SysReview>()
                .eq(SysReview::getProjectId, projectId));
    }

    @Override
    @Transactional
    public SysReview saveOrUpdateReview(SysReview review, Long userId) {
        SysReview existing = getByProjectId(review.getProjectId());
        if (existing != null) {
            review.setId(existing.getId());
            baseMapper.updateById(review);
        } else {
            review.setCreateUserId(userId);
            baseMapper.insert(review);
        }
        return review;
    }
}
