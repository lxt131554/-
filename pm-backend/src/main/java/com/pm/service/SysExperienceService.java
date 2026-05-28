package com.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pm.entity.SysExperience;
import java.util.List;

public interface SysExperienceService extends IService<SysExperience> {
    SysExperience getByProjectId(Long projectId);
    SysExperience saveOrUpdateExperience(SysExperience exp, Long userId);
    List<SysExperience> listAll();
}
