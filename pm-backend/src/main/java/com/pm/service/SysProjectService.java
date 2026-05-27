package com.pm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pm.entity.SysProject;
import com.pm.entity.SysProjectMember;
import java.util.List;

public interface SysProjectService extends IService<SysProject> {
    IPage<SysProject> pageWithMembers(int page, int size, String keyword, Long userId, String role);
    void addMember(Long projectId, Long userId, String roleInProject);
    void removeMember(Long projectId, Long memberId);
    List<SysProjectMember> getMembers(Long projectId);
}
