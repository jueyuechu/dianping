package com.dianping.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.result.Result;
import com.dianping.entity.AuditTask;
import com.dianping.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一审核中心（对齐《审核状态机详细设计.md》，骨架阶段先提供任务分页查询）
 */
@RestController
@RequestMapping("/api/admin/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    /**
     * 审核任务队列
     *
     * @param bizType 业务类型：1 商户 2 店铺 3 菜品 4 团购 5 评价 6 图片 7 帖子
     * @param status  状态：0 草稿 1 待审核 2 审核中 3 通过 4 驳回
     */
    @GetMapping("/tasks")
    public Result<Page<AuditTask>> tasks(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) Integer bizType,
            @RequestParam(required = false) Integer status) {
        return Result.success(auditService.pageTasks(current, size, bizType, status));
    }
}