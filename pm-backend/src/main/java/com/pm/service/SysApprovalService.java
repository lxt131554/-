package com.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pm.entity.SysApproval;

public interface SysApprovalService extends IService<SysApproval> {
    SysApproval getByProject(Long projectId);
    void saveOrUpdate(Long projectId, SysApproval approval, Long userId);
}
