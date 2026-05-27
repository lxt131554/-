package com.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pm.entity.SysDeviation;
import java.util.List;

public interface SysDeviationService extends IService<SysDeviation> {
    List<SysDeviation> listByProject(Long projectId);
    void closeDeviation(Long id);
    void createAutoDeviation(Long projectId, Long stageId, Long reportId, String description, String reason, Long userId);
}
