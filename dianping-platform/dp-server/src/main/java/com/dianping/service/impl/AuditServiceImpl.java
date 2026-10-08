package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.entity.AuditTask;
import com.dianping.mapper.AuditTaskMapper;
import com.dianping.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditTaskMapper auditTaskMapper;

    @Override
    public Page<AuditTask> pageTasks(long current, long size, Integer bizType, Integer status) {
        Page<AuditTask> page = new Page<>(current, size);
        LambdaQueryWrapper<AuditTask> wrapper = new LambdaQueryWrapper<AuditTask>()
                .eq(bizType != null, AuditTask::getBizType, bizType)
                .eq(status != null, AuditTask::getStatus, status)
                .orderByDesc(AuditTask::getCreateTime);
        return auditTaskMapper.selectPage(page, wrapper);
    }
}