package com.dianping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.entity.AuditTask;

public interface AuditService {

    /**
     * 统一审核任务分页查询（对齐《审核状态机详细设计.md》）
     */
    Page<AuditTask> pageTasks(long current, long size, Integer bizType, Integer status);
}