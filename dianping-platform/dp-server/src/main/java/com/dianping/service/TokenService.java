package com.dianping.service;

/**
 * 登录 token 存储：JWT 为凭证，Redis 维护有效 token 白名单（支持登出即失效）
 */
public interface TokenService {

    String SCOPE_USER = "user";
    String SCOPE_ADMIN = "admin";
    String SCOPE_MERCHANT = "merchant";

    /** 登录成功后写入 token 白名单，TTL 与 JWT 一致 */
    void save(String scope, String token, long ttlMillis);

    /** 校验 token 是否仍在白名单（Redis 不可用时降级为仅校验 JWT 签名） */
    boolean isValid(String scope, String token);

    /** 登出：从白名单移除 */
    void revoke(String scope, String token);
}
