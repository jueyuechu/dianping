package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.exception.BaseException;
import com.dianping.entity.*;
import com.dianping.mapper.*;
import com.dianping.mq.NotificationProducer;
import com.dianping.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditTaskMapper auditTaskMapper;

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private GroupbuyMapper groupbuyMapper;

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private NotificationProducer notificationProducer;

    @Override
    public Page<AuditTask> pageTasks(long current, long size, Integer bizType, Integer status) {
        Page<AuditTask> page = new Page<>(current, size);
        LambdaQueryWrapper<AuditTask> wrapper = new LambdaQueryWrapper<AuditTask>()
                .eq(bizType != null, AuditTask::getBizType, bizType)
                .eq(status != null, AuditTask::getStatus, status)
                .orderByDesc(AuditTask::getCreateTime);
        return auditTaskMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditTask submit(Integer bizType, Long bizId, Long submitterId) {
        AuditTask task = new AuditTask();
        task.setBizType(bizType);
        task.setBizId(bizId);
        task.setStatus(1);
        task.setSubmitterId(submitterId);
        auditTaskMapper.insert(task);

        writeRecord(task.getId(), submitterId, 1, "提交审核");
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pass(Long taskId, Long auditorId) {
        AuditTask task = requireProcessable(taskId);
        task.setStatus(3);
        task.setResult(1);
        task.setAuditorId(auditorId);
        task.setAuditTime(LocalDateTime.now());
        auditTaskMapper.updateById(task);

        applyBizStatus(task.getBizType(), task.getBizId(), true);
        writeRecord(taskId, auditorId, 3, "审核通过");
        notificationProducer.auditResult(task, true, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long taskId, Long auditorId, String reason) {
        AuditTask task = requireProcessable(taskId);
        task.setStatus(4);
        task.setResult(2);
        task.setReason(reason);
        task.setAuditorId(auditorId);
        task.setAuditTime(LocalDateTime.now());
        auditTaskMapper.updateById(task);

        applyBizStatus(task.getBizType(), task.getBizId(), false);
        writeRecord(taskId, auditorId, 4, reason);
        notificationProducer.auditResult(task, false, reason);
    }

    /**
     * 校验任务可审核（仅待审核/审核中）
     */
    private AuditTask requireProcessable(Long taskId) {
        AuditTask task = auditTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BaseException(404, "审核任务不存在");
        }
        if (task.getStatus() == null || (task.getStatus() != 1 && task.getStatus() != 2)) {
            throw new BaseException("当前状态不允许审核操作");
        }
        return task;
    }

    /**
     * 回写业务表：
     * 通过 → 商户激活 / 店铺营业可见 / 菜品上架 / 团购上架
     * 驳回 → 商户待审核 / 店铺休息 / 菜品下架 / 团购下架
     */
    private void applyBizStatus(Integer bizType, Long bizId, boolean pass) {
        if (bizType == null) {
            return;
        }
        switch (bizType) {
            case 1 -> {
                Merchant merchant = merchantMapper.selectById(bizId);
                if (merchant != null) {
                    merchant.setStatus(pass ? 1 : 0);
                    merchantMapper.updateById(merchant);
                }
            }
            case 2 -> {
                Shop shop = shopMapper.selectById(bizId);
                if (shop != null) {
                    // 店铺 status 兼作可见开关：1 营业（可见） 0 休息（不可见）
                    shop.setStatus(pass ? 1 : 0);
                    shopMapper.updateById(shop);
                }
            }
            case 3 -> {
                Dish dish = dishMapper.selectById(bizId);
                if (dish != null) {
                    dish.setStatus(pass ? 1 : 0);
                    dishMapper.updateById(dish);
                }
            }
            case 4 -> {
                Groupbuy groupbuy = groupbuyMapper.selectById(bizId);
                if (groupbuy != null) {
                    groupbuy.setStatus(pass ? 1 : 0);
                    groupbuyMapper.updateById(groupbuy);
                }
            }
            default -> {
                // 评价/图片：走先发后审，由举报复审处理
            }
            case 7 -> {
                // 帖子：通过恢复展示，驳回隐藏
                Blog blog = blogMapper.selectById(bizId);
                if (blog != null) {
                    blog.setStatus(pass ? 0 : 1);
                    blogMapper.updateById(blog);
                }
            }
        }
    }

    private void writeRecord(Long taskId, Long auditorId, Integer action, String comment) {
        AuditRecord record = new AuditRecord();
        record.setTaskId(taskId);
        record.setAuditorId(auditorId);
        record.setAction(action);
        record.setComment(comment);
        auditRecordMapper.insert(record);
    }
}