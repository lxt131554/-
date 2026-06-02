package com.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pm.entity.SysSupportItem;
import com.pm.mapper.SysSupportItemMapper;
import com.pm.service.SysSupportItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysSupportItemServiceImpl extends ServiceImpl<SysSupportItemMapper, SysSupportItem>
        implements SysSupportItemService {

    @Override
    public List<SysSupportItem> listAll(String status) {
        LambdaQueryWrapper<SysSupportItem> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SysSupportItem::getStatus, status);
        }
        wrapper.orderByDesc(SysSupportItem::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void resolve(Long id, String reply, String resolveNote) {
        SysSupportItem item = new SysSupportItem();
        item.setId(id);
        item.setStatus("resolved");
        item.setReply(reply);
        item.setResolveNote(resolveNote);
        baseMapper.updateById(item);
    }
}
