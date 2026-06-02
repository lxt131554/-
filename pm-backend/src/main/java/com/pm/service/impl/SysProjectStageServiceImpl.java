package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.mapper.SysProjectStageMapper;
import com.pm.mapper.SysStageReportMapper;
import com.pm.service.SysProjectStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysProjectStageServiceImpl extends ServiceImpl<SysProjectStageMapper, SysProjectStage>
        implements SysProjectStageService {

    private final SysStageReportMapper reportMapper;
    private final com.pm.mapper.SysProjectMapper projectMapper;

    @Override
    public List<SysProjectStage> listByProjectId(Long projectId) {
        List<SysProjectStage> stages = baseMapper.selectList(
            new LambdaQueryWrapper<SysProjectStage>()
                .eq(SysProjectStage::getProjectId, projectId)
                .orderByAsc(SysProjectStage::getSortOrder)
        );
        for (SysProjectStage stage : stages) {
            SysStageReport latestReport = reportMapper.selectOne(
                new LambdaQueryWrapper<SysStageReport>()
                    .eq(SysStageReport::getStageId, stage.getId())
                    .orderByDesc(SysStageReport::getCreateTime)
                    .last("LIMIT 1")
            );
            stage.setLatestReport(latestReport);
        }
        return stages;
    }

    @Override
    public List<SysProjectStage> listMyTasks(Long userId) {
        List<SysProjectStage> stages = baseMapper.selectList(
            new LambdaQueryWrapper<SysProjectStage>()
                .eq(SysProjectStage::getAssigneeId, userId)
                .orderByAsc(SysProjectStage::getStatus)
                .orderByDesc(SysProjectStage::getCreateTime)
        );
        for (SysProjectStage s : stages) {
            var project = projectMapper.selectById(s.getProjectId());
            if (project != null) s.setProjectName(project.getName());
        }
        return stages;
    }
}
