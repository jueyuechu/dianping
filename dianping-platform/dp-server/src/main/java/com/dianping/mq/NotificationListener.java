package com.dianping.mq;

import com.dianping.config.RabbitMqConfig;
import com.dianping.dto.AuditResultMessage;
import com.dianping.dto.OrderCreatedMessage;
import com.dianping.dto.OrderVerifiedMessage;
import com.dianping.entity.Message;
import com.dianping.mapper.MessageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 通知消费者：写入站内信 tb_message
 * 登录用户可在后续「消息中心」中查看；当前先完成异步落库链路
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "dianping.mq.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationListener {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private MessageMapper messageMapper;

    @RabbitListener(queues = RabbitMqConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(String body) {
        try {
            OrderCreatedMessage msg = MAPPER.readValue(body, OrderCreatedMessage.class);
            log.info("收到下单通知：orderNo={} userId={} amount={}",
                    msg.getOrderNo(), msg.getUserId(), msg.getAmount());
            saveMessage(msg.getUserId(), "下单成功：订单 " + msg.getOrderNo()
                    + "，金额 ¥" + msg.getAmount() + "，请尽快支付");
        } catch (Exception e) {
            log.error("处理下单通知失败：{}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_VERIFIED_QUEUE)
    public void onOrderVerified(String body) {
        try {
            OrderVerifiedMessage msg = MAPPER.readValue(body, OrderVerifiedMessage.class);
            log.info("收到核销通知：orderNo={} verifyCode={} userId={}",
                    msg.getOrderNo(), msg.getVerifyCode(), msg.getUserId());
            saveMessage(msg.getUserId(), "核销成功：订单 " + msg.getOrderNo()
                    + " 已于 " + msg.getVerifyTime() + " 完成核销");
        } catch (Exception e) {
            log.error("处理核销通知失败：{}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.AUDIT_QUEUE)
    public void onAuditResult(String body) {
        try {
            AuditResultMessage msg = MAPPER.readValue(body, AuditResultMessage.class);
            log.info("收到审核结果：taskId={} bizType={} passed={} submitterId={}",
                    msg.getTaskId(), msg.getBizType(), msg.getPassed(), msg.getSubmitterId());
            if (msg.getSubmitterId() != null) {
                String result = Boolean.TRUE.equals(msg.getPassed()) ? "通过" : "驳回";
                String reason = Boolean.TRUE.equals(msg.getPassed()) || msg.getReason() == null
                        ? "" : "，原因：" + msg.getReason();
                saveMessage(msg.getSubmitterId(), "您提交的" + bizLabel(msg.getBizType())
                        + "审核" + result + reason);
            }
        } catch (Exception e) {
            log.error("处理审核通知失败：{}", e.getMessage(), e);
        }
    }

    private void saveMessage(Long userId, String content) {
        if (userId == null) {
            return;
        }
        Message message = new Message();
        message.setUserId(userId);
        message.setType(1);
        message.setContent(content);
        message.setIsRead(0);
        messageMapper.insert(message);
    }

    private String bizLabel(Integer bizType) {
        if (bizType == null) {
            return "内容";
        }
        return switch (bizType) {
            case 1 -> "商户入驻";
            case 2 -> "店铺";
            case 3 -> "菜品";
            case 4 -> "团购";
            case 5 -> "评价";
            case 6 -> "图片";
            case 7 -> "帖子";
            default -> "内容";
        };
    }
}
