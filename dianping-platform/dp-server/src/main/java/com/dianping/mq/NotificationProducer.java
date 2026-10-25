package com.dianping.mq;

import com.dianping.common.properties.MqProperties;
import com.dianping.config.RabbitMqConfig;
import com.dianping.dto.AuditResultMessage;
import com.dianping.dto.OrderCreatedMessage;
import com.dianping.dto.OrderVerifiedMessage;
import com.dianping.entity.AuditTask;
import com.dianping.entity.GroupbuyOrder;
import com.dianping.entity.VerificationRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 通知生产者：下单 / 核销 / 审核结果
 * RabbitMQ 不可用时只记录告警，不影响主业务
 */
@Slf4j
@Service
public class NotificationProducer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MqProperties mqProperties;

    public void orderCreated(GroupbuyOrder order) {
        send(RabbitMqConfig.RK_ORDER_CREATED, OrderCreatedMessage.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .shopId(order.getShopId())
                .amount(order.getAmount())
                .createTime(now())
                .build());
    }

    public void orderVerified(GroupbuyOrder order, VerificationRecord record) {
        send(RabbitMqConfig.RK_ORDER_VERIFIED, OrderVerifiedMessage.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .verifyCode(order.getVerifyCode())
                .userId(order.getUserId())
                .shopId(order.getShopId())
                .operatorId(record.getOperatorId())
                .verifyTime(record.getVerifyTime() == null
                        ? now() : record.getVerifyTime().format(TIME_FORMAT))
                .build());
    }

    public void auditResult(AuditTask task, boolean passed, String reason) {
        send(RabbitMqConfig.RK_AUDIT_RESULT, AuditResultMessage.builder()
                .taskId(task.getId())
                .bizType(task.getBizType())
                .bizId(task.getBizId())
                .passed(passed)
                .reason(reason)
                .submitterId(task.getSubmitterId())
                .auditTime(now())
                .build());
    }

    private void send(String routingKey, Object payload) {
        if (!mqProperties.isEnabled()) {
            log.info("MQ 已禁用，跳过通知：{}", routingKey);
            return;
        }
        try {
            String body = MAPPER.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, routingKey, body);
            log.info("MQ 通知已发送：{}", routingKey);
        } catch (Exception e) {
            log.warn("RabbitMQ 通知发送失败（业务不受影响）：{} - {}", routingKey, e.getMessage());
        }
    }

    private String now() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }
}
