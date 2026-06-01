package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import com.pm.mapper.SysProjectMapper;
import com.pm.mapper.SysProjectMemberMapper;
import com.pm.service.SysProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysProjectServiceImpl extends ServiceImpl<SysProjectMapper, SysProject> implements SysProjectService {

    private final SysProjectMemberMapper memberMapper;

    @Override
    public IPage<SysProject> pageWithMembers(int page, int size, String keyword, Long userId, String role) {
        LambdaQueryWrapper<SysProject> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysProject::getName, keyword);
        }
        if (!"admin".equals(role) && !"leader".equals(role)) {
            List<SysProjectMember> myMembers = memberMapper.selectList(
                new LambdaQueryWrapper<SysProjectMember>().eq(SysProjectMember::getUserId, userId)
            );
            Set<Long> myProjectIds = myMembers.stream().map(SysProjectMember::getProjectId).collect(Collectors.toSet());
            if (myProjectIds.isEmpty()) {
                wrapper.eq(SysProject::getId, -1L);
            } else {
                wrapper.in(SysProject::getId, myProjectIds);
            }
        }
        wrapper.orderByDesc(SysProject::getCreateTime);
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public void addMember(Long projectId, Long userId, String roleInProject) {
        SysProjectMember member = new SysProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRoleInProject(roleInProject);
        memberMapper.insert(member);
    }

    @Override
    public void removeMember(Long projectId, Long memberId) {
        memberMapper.deleteById(memberId);
    }

    @Override
    public List<SysProjectMember> getMembers(Long projectId) {
        return memberMapper.selectList(
            new LambdaQueryWrapper<SysProjectMember>().eq(SysProjectMember::getProjectId, projectId)
        );
    }
}
