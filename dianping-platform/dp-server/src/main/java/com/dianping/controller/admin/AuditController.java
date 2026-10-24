package com.dianping.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.context.BaseContext;
import com.dianping.common.result.Result;
import com.dianping.dto.AuditRejectDTO;
import com.dianping.entity.AuditRecord;
import com.dianping.entity.AuditTask;
import com.dianping.mapper.AuditRecordMapper;
import com.dianping.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 统一审核中心（对齐《审核状态机详细设计.md》）
 */
@RestController
@RequestMapping("/api/admin/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditRecordMapper auditRecordMapper;

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

    /**
     * 审核通过（同时回写业务表状态并留痕）
     */
    @PostMapping("/{taskId}/pass")
    public Result<Void> pass(@PathVariable Long taskId) {
        auditService.pass(taskId, BaseContext.getCurrentId());
        return Result.success();
    }

    /**
     * 审核驳回（需填写原因，同时回写业务表状态并留痕）
     */
    @PostMapping("/{taskId}/reject")
    public Result<Void> reject(@PathVariable Long taskId, @RequestBody @Valid AuditRejectDTO dto) {
        auditService.reject(taskId, BaseContext.getCurrentId(), dto.getReason());
        return Result.success();
    }

    /**
     * 审核记录（按任务查询）
     */
    @GetMapping("/records")
    public Result<Page<AuditRecord>> records(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "20") long size,
                                             @RequestParam(required = false) Long taskId) {
        return Result.success(auditRecordMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<AuditRecord>()
                        .eq(taskId != null, AuditRecord::getTaskId, taskId)
                        .orderByDesc(AuditRecord::getCreateTime)));
    }
}