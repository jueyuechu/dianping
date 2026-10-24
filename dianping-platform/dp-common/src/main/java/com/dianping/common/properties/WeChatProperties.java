package com.dianping.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置：appid / secret，mock 为 true 时不调用微信接口（本地开发）
 */
@Component
@ConfigurationProperties(prefix = "dianping.wechat")
@Data
public class WeChatProperties {

    private String appid;

    private String secret;

    /** 是否使用模拟登录（本地开发默认 true） */
    private boolean mock = true;
}