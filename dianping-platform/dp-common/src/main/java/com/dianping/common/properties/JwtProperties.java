package com.dianping.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置：三端分离密钥与有效期（对齐苍穹外卖 JwtProperties）
 */
@Component
@ConfigurationProperties(prefix = "dianping.jwt")
@Data
public class JwtProperties {

    /** 用户端密钥 */
    private String userSecretKey;
    /** 用户端 token 有效期（毫秒），默认 7 天 */
    private Long userTtl = 604800000L;

    /** 管理员端密钥 */
    private String adminSecretKey;
    /** 管理员端 token 有效期（毫秒），默认 2 小时 */
    private Long adminTtl = 7200000L;

    /** 商户端密钥 */
    private String merchantSecretKey;
    /** 商户端 token 有效期（毫秒），默认 2 小时 */
    private Long merchantTtl = 7200000L;
}