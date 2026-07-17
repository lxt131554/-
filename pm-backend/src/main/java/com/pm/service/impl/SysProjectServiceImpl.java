package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.entity.SysChange;
import com.pm.mapper.SysChangeMapper;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.mapper.SysUserMapper;
import com.pm.service.SysProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SysProjectServiceImpl extends ServiceImpl<SysProjectMapper, SysProject> implements SysProjectService {

    private final SysProjectMemberMapper memberMapper;
    private final SysUserMapper userMapper;
    private final SysChangeMapper changeMapper;

    @Override
    public IPage<SysProject> pageWithMembers(int page, int size, String keyword, String status, Long userId, String role) {
        LambdaQueryWrapper<SysProject> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysProject::getName, keyword);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SysProject::getStatus, status);
        }
        if (!"admin".equals(role) && !"leader".equals(role)) {
            List<SysProjectMember> myMembers = memberMapper.selectList(
                new LambdaQueryWrapper<SysProjectMember>()
                    .eq(SysProjectMember::getUserId, userId)
                    .eq(SysProjectMember::getStatus, "confirmed")
            );
            Set<Long> myProjectIds = myMembers.stream().map(SysProjectMember::getProjectId).collect(Collectors.toSet());
            if (myProjectIds.isEmpty()) {
                wrapper.eq(SysProject::getId, -1L);
            } else {
                wrapper.in(SysProject::getId, myProjectIds);
            }
        }
        wrapper.orderByDesc(SysProject::getCreateTime);
        IPage<SysProject> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        fillManagerNames(result.getRecords());
        return result;
    }

    @Override
    @Transactional
    public void addMember(Long projectId, Long userId, String roleInProject, String status) {
        SysProjectMember member = new SysProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRoleInProject(roleInProject);
        member.setStatus(status != null ? status : "pending");
        memberMapper.insert(member);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long memberId, String reason, Long operatorUserId) {
        SysProjectMember member = memberMapper.selectById(memberId);
        if (member == null || !projectId.equals(member.getProjectId())) {
            throw new IllegalArgumentException("成员不属于该项目");
        }
        String memberName = "用户" + member.getUserId();
        var memberUser = userMapper.selectById(member.getUserId());
        if (memberUser != null && memberUser.getRealName() != null && !memberUser.getRealName().trim().isEmpty()) {
            memberName = memberUser.getRealName();
        }
        memberMapper.deleteById(memberId);

        if (!"confirmed".equals(member.getStatus())) {
            return;
        }

        SysChange change = new SysChange();
        change.setProjectId(projectId);
        change.setContent("移除项目成员：" + memberName + "（"
                + ("manager".equals(member.getRoleInProject()) ? "负责人" : "工程师") + "）");
        change.setImpact("移除原因：" + reason.trim());
        change.setStatus("confirmed");
        change.setConfirmTime(LocalDate.now());
        change.setCreateUserId(operatorUserId);
        changeMapper.insert(change);
    }

    @Override
    public List<SysProjectMember> getMembers(Long projectId) {
        return memberMapper.selectList(
            new LambdaQueryWrapper<SysProjectMember>().eq(SysProjectMember::getProjectId, projectId)
        );
    }

    private void fillManagerNames(List<SysProject> projects) {
        if (projects == null || projects.isEmpty()) {
            return;
        }
        Set<Long> projectIds = projects.stream().map(SysProject::getId).collect(Collectors.toSet());
        List<SysProjectMember> managers = memberMapper.selectList(new LambdaQueryWrapper<SysProjectMember>()
                .in(SysProjectMember::getProjectId, projectIds)
                .eq(SysProjectMember::getRoleInProject, "manager")
                .eq(SysProjectMember::getStatus, "confirmed"));

        Set<Long> userIds = new HashSet<>();
        for (SysProjectMember manager : managers) {
            if (manager.getUserId() != null) {
                userIds.add(manager.getUserId());
            }
        }
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds)
                    .forEach(user -> userNameMap.put(user.getId(), user.getRealName()));
        }

        Map<Long, String> projectManagerMap = new HashMap<>();
        for (SysProjectMember manager : managers) {
            projectManagerMap.putIfAbsent(manager.getProjectId(), userNameMap.getOrDefault(manager.getUserId(), ""));
        }
        for (SysProject project : projects) {
            project.setManagerName(projectManagerMap.getOrDefault(project.getId(), ""));
        }
    }
}
