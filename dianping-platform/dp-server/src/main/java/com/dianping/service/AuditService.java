package com.dianping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.entity.AuditTask;

public interface AuditService {

    /**
     * 统一审核任务分页查询（对齐《审核状态机详细设计.md》）
     */
    Page<AuditTask> pageTasks(long current, long size, Integer bizType, Integer status);

    /**
     * 提交审核：写入 tb_audit_task（状态 1 待审核）
     *
     * @param bizType 1 商户 2 店铺 3 菜品 4 团购 5 评价 6 图片 7 帖子
     */
    AuditTask submit(Integer bizType, Long bizId, Long submitterId);

    /**
     * 审核通过：任务 → 通过，并回写业务表为可上线状态
     */
    void pass(Long taskId, Long auditorId);

    /**
     * 审核驳回：任务 → 驳回，并回写业务表为不可见状态
     */
    void reject(Long taskId, Long auditorId, String reason);
}