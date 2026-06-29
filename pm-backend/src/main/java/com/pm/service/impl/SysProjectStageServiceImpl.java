package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysProjectStage;
import com.pm.entity.SysStageReport;
import com.pm.mapper.SysProjectStageMapper;
import com.pm.mapper.SysStageReportMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.service.SysProjectStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysProjectStageServiceImpl extends ServiceImpl<SysProjectStageMapper, SysProjectStage>
        implements SysProjectStageService {

    private final SysStageReportMapper reportMapper;
    private final com.pm.mapper.SysProjectMapper projectMapper;
    private final SysUserMapper userMapper;

    @Override
    public List<SysProjectStage> listByProjectId(Long projectId) {
        List<SysProjectStage> stages = baseMapper.selectList(
            new LambdaQueryWrapper<SysProjectStage>()
                .eq(SysProjectStage::getProjectId, projectId)
                .orderByAsc(SysProjectStage::getSortOrder)
        );
        String projectName = null;
        var project = projectMapper.selectById(projectId);
        if (project != null) {
            projectName = project.getName();
        }
        Map<Long, String> assigneeNameMap = buildUserNameMap(stages.stream()
                .map(SysProjectStage::getAssigneeId)
                .collect(Collectors.toSet()));
        for (SysProjectStage stage : stages) {
            stage.setProjectName(projectName);
            stage.setAssigneeName(assigneeNameMap.getOrDefault(stage.getAssigneeId(), ""));
            SysStageReport latestReport = reportMapper.selectOne(
                new LambdaQueryWrapper<SysStageReport>()
                    .eq(SysStageReport::getStageId, stage.getId())
                    .orderByDesc(SysStageReport::getCreateTime)
                    .last("LIMIT 1")
            );
            if (latestReport != null) {
                latestReport.setAttachmentData(null);
            }
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
        Map<Long, String> projectNameMap = new HashMap<>();
        Set<Long> projectIds = new HashSet<>();
        for (SysProjectStage stage : stages) {
            if (stage.getProjectId() != null) {
                projectIds.add(stage.getProjectId());
            }
        }
        Map<Long, String> projectStatusMap = new HashMap<>();
        if (!projectIds.isEmpty()) {
            projectMapper.selectBatchIds(projectIds)
                    .forEach(project -> {
                        projectNameMap.put(project.getId(), project.getName());
                        projectStatusMap.put(project.getId(), project.getStatus());
                    });
        }
        Set<Long> assigneeIds = new HashSet<>();
        assigneeIds.add(userId);
        Map<Long, String> assigneeNameMap = buildUserNameMap(assigneeIds);
        List<SysProjectStage> result = new ArrayList<>();
        for (SysProjectStage s : stages) {
            // Skip stages from completed projects
            if ("completed".equals(projectStatusMap.get(s.getProjectId()))) {
                continue;
            }
            s.setProjectName(projectNameMap.getOrDefault(s.getProjectId(), ""));
            s.setAssigneeName(assigneeNameMap.getOrDefault(s.getAssigneeId(), ""));
            result.add(s);
        }
        return result;
    }

    private Map<Long, String> buildUserNameMap(Set<Long> userIds) {
        Map<Long, String> userNameMap = new HashMap<>();
        Set<Long> nonNullUserIds = new HashSet<>();
        for (Long userId : userIds) {
            if (userId != null) {
                nonNullUserIds.add(userId);
            }
        }
        if (!nonNullUserIds.isEmpty()) {
            userMapper.selectBatchIds(nonNullUserIds)
                    .forEach(user -> userNameMap.put(user.getId(), user.getRealName()));
        }
        return userNameMap;
    }
}
