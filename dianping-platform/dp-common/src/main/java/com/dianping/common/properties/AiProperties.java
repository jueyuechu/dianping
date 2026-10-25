package com.dianping.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 配置：mock=true 时使用内置规则生成 SQL，便于无 Key 本地演示
 * LLM 连接参数由 Spring AI 管理（spring.ai.openai.*）
 */
@Component
@ConfigurationProperties(prefix = "dianping.ai")
@Data
public class AiProperties {

    private boolean mock = true;

    /** 查询最大返回行数 */
    private int maxRows = 50;
}