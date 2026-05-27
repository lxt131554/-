package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysDeviation;
import com.pm.mapper.SysDeviationMapper;
import com.pm.service.SysDeviationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysDeviationServiceImpl extends ServiceImpl<SysDeviationMapper, SysDeviation>
        implements SysDeviationService {

    @Override
    public List<SysDeviation> listByProject(Long projectId) {
        LambdaQueryWrapper<SysDeviation> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(SysDeviation::getProjectId, projectId);
        }
        wrapper.orderByDesc(SysDeviation::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void closeDeviation(Long id) {
        SysDeviation d = new SysDeviation();
        d.setId(id);
        d.setStatus("closed");
        d.setCloseTime(LocalDateTime.now());
        baseMapper.updateById(d);
    }

    @Override
    public void createAutoDeviation(Long projectId, Long stageId, Long reportId,
                                    String description, String reason, Long userId) {
        SysDeviation d = new SysDeviation();
        d.setProjectId(projectId);
        d.setStageId(stageId);
        d.setReportId(reportId);
        d.setType("auto");
        d.setDescription(description);
        d.setReason(reason);
        d.setStatus("open");
        d.setCreateUserId(userId);
        baseMapper.insert(d);
    }
}
