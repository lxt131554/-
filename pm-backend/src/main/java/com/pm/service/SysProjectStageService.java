package com.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pm.entity.SysProjectStage;
import java.util.List;

public interface SysProjectStageService extends IService<SysProjectStage> {
    List<SysProjectStage> listByProjectId(Long projectId);
    List<SysProjectStage> listMyTasks(Long userId);
}
