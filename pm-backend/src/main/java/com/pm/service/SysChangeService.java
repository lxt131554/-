package com.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pm.entity.SysChange;
import java.util.List;

public interface SysChangeService extends IService<SysChange> {
    List<SysChange> listByProject(Long projectId);
    void confirm(Long id);
}
