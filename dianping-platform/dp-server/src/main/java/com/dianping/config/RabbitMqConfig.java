package com.dianping.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 拓扑：单一 topic 交换机 + 三个业务队列
 * 下单成功 / 核销完成 / 审核结果 分别独立消费，便于单独扩容与重试
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "dianping.notify";

    public static final String ORDER_CREATED_QUEUE = "dianping.notify.order-created";
    public static final String ORDER_VERIFIED_QUEUE = "dianping.notify.order-verified";
    public static final String AUDIT_QUEUE = "dianping.notify.audit";

    public static final String RK_ORDER_CREATED = "order.created";
    public static final String RK_ORDER_VERIFIED = "order.verified";
    public static final String RK_AUDIT_RESULT = "audit.result";

    @Bean
    public TopicExchange notifyExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue orderVerifiedQueue() {
        return QueueBuilder.durable(ORDER_VERIFIED_QUEUE).build();
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange notifyExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(notifyExchange).with(RK_ORDER_CREATED);
    }

    @Bean
    public Binding orderVerifiedBinding(Queue orderVerifiedQueue, TopicExchange notifyExchange) {
        return BindingBuilder.bind(orderVerifiedQueue).to(notifyExchange).with(RK_ORDER_VERIFIED);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange notifyExchange) {
        return BindingBuilder.bind(auditQueue).to(notifyExchange).with(RK_AUDIT_RESULT);
    }
}
