package com.dianping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.entity.Message;

/**
 * 站内消息：MQ 通知落库后的查询与已读管理
 */
public interface MessageService {

    Page<Message> page(long current, long size, Integer isRead, Long userId);

    long unreadCount(Long userId);

    void markRead(Long id, Long userId);

    void markAllRead(Long userId);
}
