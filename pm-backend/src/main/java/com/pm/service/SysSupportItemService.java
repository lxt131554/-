package com.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pm.entity.SysSupportItem;
import java.util.List;

public interface SysSupportItemService extends IService<SysSupportItem> {
    List<SysSupportItem> listAll(String status);
    void resolve(Long id, String reply, String resolveNote);
}
