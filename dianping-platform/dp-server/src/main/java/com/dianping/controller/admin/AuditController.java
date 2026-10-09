package com.dianping.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.context.BaseContext;
import com.dianping.common.result.Result;
import com.dianping.dto.AuditRejectDTO;
import com.dianping.entity.AuditTask;
import com.dianping.mapper.AuditTaskMapper;
import com.dianping.service.AuditService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 统一审核中心（对齐《审核状态机详细设计.md》）
 */
@RestController
@RequestMapping("/api/admin/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditTaskMapper auditTaskMapper;

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
     * 审核通过：待审核/审核中 → 通过
     */
    @PostMapping("/{taskId}/pass")
    public Result<Void> pass(@PathVariable Long taskId) {
        AuditTask task = auditTaskMapper.selectById(taskId);
        if (task == null) {
            return Result.error(404, "审核任务不存在");
        }
        if (task.getStatus() != 1 && task.getStatus() != 2) {
            return Result.error("当前状态不允许通过操作");
        }
        task.setStatus(3);
        task.setResult(1);
        task.setAuditorId(BaseContext.getCurrentId());
        task.setAuditTime(LocalDateTime.now());
        auditTaskMapper.updateById(task);
        return Result.success();
    }

    /**
     * 审核驳回：待审核/审核中 → 驳回（需填写原因）
     */
    @PostMapping("/{taskId}/reject")
    public Result<Void> reject(@PathVariable Long taskId, @RequestBody @Valid AuditRejectDTO dto) {
        AuditTask task = auditTaskMapper.selectById(taskId);
        if (task == null) {
            return Result.error(404, "审核任务不存在");
        }
        if (task.getStatus() != 1 && task.getStatus() != 2) {
            return Result.error("当前状态不允许驳回操作");
        }
        task.setStatus(4);
        task.setResult(2);
        task.setReason(dto.getReason());
        task.setAuditorId(BaseContext.getCurrentId());
        task.setAuditTime(LocalDateTime.now());
        auditTaskMapper.updateById(task);
        return Result.success();
    }
}