package com.dianping.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置：mock=false 时才启用 ChatClient（LLM 模式）
 */
@Configuration
public class AiConfig {

    @Bean
    @ConditionalOnProperty(name = "dianping.ai.mock", havingValue = "false")
    public ChatClient text2SqlChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
