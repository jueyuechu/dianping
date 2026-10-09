package com.dianping.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.result.Result;
import com.dianping.entity.Report;
import com.dianping.mapper.ReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端举报处理
 */
@RestController
@RequestMapping("/api/admin/report")
public class AdminReportController {

    @Autowired
    private ReportMapper reportMapper;

    @GetMapping("/list")
    public Result<Page<Report>> list(@RequestParam(defaultValue = "1") long current,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) Integer targetType,
                                     @RequestParam(required = false) Integer handled) {
        Page<Report> page = new Page<>(current, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<Report>()
                .eq(targetType != null, Report::getTargetType, targetType)
                .isNull(handled != null && handled == 0, Report::getResult)
                .isNotNull(handled != null && handled == 1, Report::getResult)
                .orderByDesc(Report::getCreateTime);
        return Result.success(reportMapper.selectPage(page, wrapper));
    }

    /**
     * 举报处理：result 1 驳回 2 删除内容 3 警告 4 封禁
     */
    @PostMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @RequestParam Integer result) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return Result.error(404, "举报不存在");
        }
        report.setResult(result);
        reportMapper.updateById(report);
        return Result.success();
    }
}