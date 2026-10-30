package com.dianping.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.result.Result;
import com.dianping.service.RefundService;
import com.dianping.vo.RefundVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端退款审核
 */
@RestController
@RequestMapping("/api/admin/refund")
public class AdminRefundController {

    @Autowired
    private RefundService refundService;

    @GetMapping("/list")
    public Result<Page<RefundVO>> list(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Integer status) {
        return Result.success(refundService.adminPage(current, size, status));
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        refundService.approve(id);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestParam(required = false) String reason) {
        refundService.reject(id, reason);
        return Result.success();
    }
}
