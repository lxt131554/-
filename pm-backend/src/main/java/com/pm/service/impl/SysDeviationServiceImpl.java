package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysDeviation;
import com.pm.mapper.SysDeviationMapper;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysProjectStageMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.service.SysDeviationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysDeviationServiceImpl extends ServiceImpl<SysDeviationMapper, SysDeviation>
        implements SysDeviationService {

    private final SysProjectMapper projectMapper;
    private final SysProjectStageMapper stageMapper;
    private final SysUserMapper userMapper;

    @Override
    public List<SysDeviation> listByProject(Long projectId) {
        LambdaQueryWrapper<SysDeviation> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(SysDeviation::getProjectId, projectId);
        }
        wrapper.orderByDesc(SysDeviation::getCreateTime);
        return enrich(baseMapper.selectList(wrapper));
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

    private List<SysDeviation> enrich(List<SysDeviation> items) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        Set<Long> projectIds = new HashSet<>();
        Set<Long> stageIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        for (SysDeviation item : items) {
            if (item.getProjectId() != null) {
                projectIds.add(item.getProjectId());
            }
            if (item.getStageId() != null) {
                stageIds.add(item.getStageId());
            }
            if (item.getCreateUserId() != null) {
                userIds.add(item.getCreateUserId());
            }
        }

        Map<Long, String> projectNameMap = new HashMap<>();
        if (!projectIds.isEmpty()) {
            projectMapper.selectBatchIds(projectIds)
                    .forEach(project -> projectNameMap.put(project.getId(), project.getName()));
        }

        Map<Long, String> stageNameMap = new HashMap<>();
        if (!stageIds.isEmpty()) {
            stageMapper.selectBatchIds(stageIds)
                    .forEach(stage -> stageNameMap.put(stage.getId(), stage.getStageName()));
        }

        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds)
                    .forEach(user -> userNameMap.put(user.getId(), user.getRealName()));
        }

        for (SysDeviation item : items) {
            item.setProjectName(projectNameMap.getOrDefault(item.getProjectId(), ""));
            item.setStageName(stageNameMap.getOrDefault(item.getStageId(), ""));
            item.setCreateUserName(userNameMap.getOrDefault(item.getCreateUserId(), ""));
        }
        return items;
    }
}
