package com.dianping.interceptor;

import com.dianping.common.context.BaseContext;
import com.dianping.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录拦截器：解析 Authorization 头（支持 Bearer 前缀），校验通过后写入 BaseContext
 */
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final String secretKey;

    /** token 中的身份标识 key（userId / adminId / merchantId） */
    private final String claimName;

    public JwtTokenInterceptor(String secretKey, String claimName) {
        this.secretKey = secretKey;
        this.claimName = claimName;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isBlank()) {
            writeUnauthorized(response);
            return false;
        }
        try {
            Claims claims = JwtUtil.parseJWT(secretKey, token);
            Long id = claims.get(claimName, Long.class);
            if (id == null) {
                writeUnauthorized(response);
                return false;
            }
            BaseContext.setCurrentId(id);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("token 已过期：{}", e.getMessage());
            writeUnauthorized(response);
            return false;
        } catch (Exception e) {
            log.debug("token 校验失败：{}", e.getMessage());
            writeUnauthorized(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\",\"data\":null}");
    }
}