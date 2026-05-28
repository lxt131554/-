package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysExperience;
import com.pm.mapper.SysExperienceMapper;
import com.pm.service.SysExperienceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysExperienceServiceImpl extends ServiceImpl<SysExperienceMapper, SysExperience>
        implements SysExperienceService {

    @Override
    public SysExperience getByProjectId(Long projectId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SysExperience>()
                .eq(SysExperience::getProjectId, projectId));
    }

    @Override
    @Transactional
    public SysExperience saveOrUpdateExperience(SysExperience exp, Long userId) {
        SysExperience existing = getByProjectId(exp.getProjectId());
        if (existing != null) {
            exp.setId(existing.getId());
            baseMapper.updateById(exp);
        } else {
            exp.setCreateUserId(userId);
            baseMapper.insert(exp);
        }
        return exp;
    }

    @Override
    public List<SysExperience> listAll() {
        LambdaQueryWrapper<SysExperience> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysExperience::getCreateTime);
        return baseMapper.selectList(wrapper);
    }
}
