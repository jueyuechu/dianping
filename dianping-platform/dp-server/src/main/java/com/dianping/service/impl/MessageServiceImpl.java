package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.entity.Message;
import com.dianping.mapper.MessageMapper;
import com.dianping.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public Page<Message> page(long current, long size, Integer isRead, Long userId) {
        Page<Message> page = new Page<>(current, size);
        return messageMapper.selectPage(page, new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(isRead != null, Message::getIsRead, isRead)
                .orderByDesc(Message::getCreateTime));
    }

    @Override
    public long unreadCount(Long userId) {
        return messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    @Override
    public void markRead(Long id, Long userId) {
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getId, id)
                .eq(Message::getUserId, userId)
                .set(Message::getIsRead, 1));
    }

    @Override
    public void markAllRead(Long userId) {
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1));
    }
}
