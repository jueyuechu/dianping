package com.dianping.service.impl;

import com.dianping.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Redis token 白名单实现
 * key: dianping:token:{scope}:{token}，value: 1，TTL 与 JWT 有效期一致
 * Redis 不可用时记录一次告警并降级为仅校验 JWT 签名，避免本地未启动 Redis 时整个系统不可用
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private static final String KEY_PREFIX = "dianping:token:";

    private final AtomicBoolean redisUnavailableLogged = new AtomicBoolean(false);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(String scope, String token, long ttlMillis) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue()
                    .set(key(scope, token), "1", Duration.ofMillis(ttlMillis));
        } catch (Exception e) {
            warnRedisUnavailable(e);
        }
    }

    @Override
    public boolean isValid(String scope, String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key(scope, token)));
        } catch (Exception e) {
            warnRedisUnavailable(e);
            // 降级：Redis 不可用时仍信任已验签的 JWT
            return true;
        }
    }

    @Override
    public void revoke(String scope, String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            stringRedisTemplate.delete(key(scope, token));
        } catch (Exception e) {
            warnRedisUnavailable(e);
        }
    }

    private String key(String scope, String token) {
        return KEY_PREFIX + scope + ":" + token;
    }

    private void warnRedisUnavailable(Exception e) {
        if (redisUnavailableLogged.compareAndSet(false, true)) {
            log.warn("Redis 不可用，token 白名单降级为仅校验 JWT：{}", e.getMessage());
        }
    }
}
