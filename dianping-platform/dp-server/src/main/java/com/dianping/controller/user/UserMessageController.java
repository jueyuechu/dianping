package com.dianping.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.common.result.Result;
import com.dianping.entity.Message;
import com.dianping.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端消息中心
 */
@RestController
@RequestMapping("/api/user/message")
public class UserMessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/list")
    public Result<Page<Message>> list(@RequestParam(defaultValue = "1") long current,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) Integer isRead) {
        return Result.success(messageService.page(current, size, isRead, currentUserId()));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.unreadCount(currentUserId()));
    }

    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        messageService.markRead(id, currentUserId());
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> readAll() {
        messageService.markAllRead(currentUserId());
        return Result.success();
    }

    private Long currentUserId() {
        Long id = BaseContext.getCurrentId();
        if (id == null) {
            throw new BaseException(401, "未登录或登录已过期");
        }
        return id;
    }
}
