package com.dianping.controller.merchant;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.common.result.Result;
import com.dianping.entity.Message;
import com.dianping.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商户端消息中心（审核结果通知等）
 */
@RestController
@RequestMapping("/api/merchant/message")
public class MerchantMessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/list")
    public Result<Page<Message>> list(@RequestParam(defaultValue = "1") long current,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) Integer isRead) {
        return Result.success(messageService.page(current, size, isRead, currentMerchantId()));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.unreadCount(currentMerchantId()));
    }

    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        messageService.markRead(id, currentMerchantId());
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> readAll() {
        messageService.markAllRead(currentMerchantId());
        return Result.success();
    }

    private Long currentMerchantId() {
        Long id = BaseContext.getCurrentId();
        if (id == null) {
            throw new BaseException(401, "未登录或登录已过期");
        }
        return id;
    }
}
