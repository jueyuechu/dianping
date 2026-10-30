package com.dianping.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.common.result.Result;
import com.dianping.dto.RefundApplyDTO;
import com.dianping.service.RefundService;
import com.dianping.vo.RefundVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端退款申请
 */
@RestController
@RequestMapping("/api/user/refund")
public class UserRefundController {

    @Autowired
    private RefundService refundService;

    @PostMapping("/apply")
    public Result<Long> apply(@RequestBody RefundApplyDTO dto) {
        return Result.success(refundService.apply(currentUserId(), dto.getOrderId(), dto.getReason()));
    }

    @GetMapping("/list")
    public Result<Page<RefundVO>> list(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size) {
        return Result.success(refundService.myPage(current, size, currentUserId()));
    }

    private Long currentUserId() {
        Long id = BaseContext.getCurrentId();
        if (id == null) {
            throw new BaseException(401, "未登录或登录已过期");
        }
        return id;
    }
}
