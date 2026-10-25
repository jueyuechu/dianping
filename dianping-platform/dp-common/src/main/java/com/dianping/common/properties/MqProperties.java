package com.dianping.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 消息队列配置：enabled=false 时跳过发送与消费，便于本地未启动 RabbitMQ 时演示
 */
@Component
@ConfigurationProperties(prefix = "dianping.mq")
@Data
public class MqProperties {

    private boolean enabled = true;
}
