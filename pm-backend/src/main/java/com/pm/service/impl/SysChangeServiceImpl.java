package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysChange;
import com.pm.mapper.SysChangeMapper;
import com.pm.service.SysChangeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysChangeServiceImpl extends ServiceImpl<SysChangeMapper, SysChange>
        implements SysChangeService {

    @Override
    public List<SysChange> listByProject(Long projectId) {
        return baseMapper.selectList(new LambdaQueryWrapper<SysChange>()
                .eq(SysChange::getProjectId, projectId)
                .orderByDesc(SysChange::getCreateTime));
    }

    @Override
    @Transactional
    public void confirm(Long id) {
        SysChange c = new SysChange();
        c.setId(id);
        c.setStatus("confirmed");
        baseMapper.updateById(c);
    }
}
