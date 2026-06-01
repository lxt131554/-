package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysApproval;
import com.pm.mapper.SysApprovalMapper;
import com.pm.service.SysApprovalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysApprovalServiceImpl extends ServiceImpl<SysApprovalMapper, SysApproval>
        implements SysApprovalService {

    @Override
    public SysApproval getByProject(Long projectId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SysApproval>()
                .eq(SysApproval::getProjectId, projectId));
    }

    @Override
    @Transactional
    public void saveOrUpdate(Long projectId, SysApproval approval, Long userId) {
        SysApproval existing = getByProject(projectId);
        if (existing != null) {
            approval.setId(existing.getId());
            baseMapper.updateById(approval);
        } else {
            approval.setProjectId(projectId);
            approval.setCreateUserId(userId);
            baseMapper.insert(approval);
        }
    }
}
