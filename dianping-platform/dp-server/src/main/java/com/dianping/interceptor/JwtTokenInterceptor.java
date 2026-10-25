package com.dianping.interceptor;

import com.dianping.common.context.BaseContext;
import com.dianping.common.utils.JwtUtil;
import com.dianping.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * JWT 登录拦截器：校验 Authorization 头 + Redis token 白名单，通过后写入 BaseContext
 */
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final String secretKey;

    /** token 中的身份标识 key（userId / adminId / merchantId） */
    private final String claimName;

    /** token 白名单作用域（user / admin / merchant） */
    private final String tokenScope;

    private final TokenService tokenService;

    /** 允许匿名访问的 GET 路径前缀（如团购详情浏览，写操作仍需登录） */
    private final List<String> anonymousGetPrefixes;

    public JwtTokenInterceptor(String secretKey, String claimName, String tokenScope,
                               TokenService tokenService, List<String> anonymousGetPrefixes) {
        this.secretKey = secretKey;
        this.claimName = claimName;
        this.tokenScope = tokenScope;
        this.tokenService = tokenService;
        this.anonymousGetPrefixes = anonymousGetPrefixes;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 浏览器跨域预检请求不含 token，必须直接放行，否则 CORS 失败
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        boolean anonymousGet = false;
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String uri = request.getRequestURI();
            for (String prefix : anonymousGetPrefixes) {
                if (uri.startsWith(prefix)) {
                    anonymousGet = true;
                    break;
                }
            }
        }

        String token = JwtUtil.resolveToken(request.getHeader("Authorization"));

        // 匿名浏览接口：token 有效时写入上下文（用于 liked 等个性化字段），无效则直接放行
        if (anonymousGet) {
            if (token != null && !token.isBlank()) {
                try {
                    Claims claims = JwtUtil.parseJWT(secretKey, token);
                    Long id = claims.get(claimName, Long.class);
                    if (id != null && tokenService.isValid(tokenScope, token)) {
                        BaseContext.setCurrentId(id);
                    }
                } catch (Exception e) {
                    log.debug("匿名接口 token 解析失败（忽略）：{}", e.getMessage());
                }
            }
            return true;
        }

        if (token == null || token.isBlank()) {
            writeUnauthorized(response);
            return false;
        }
        try {
            Claims claims = JwtUtil.parseJWT(secretKey, token);
            Long id = claims.get(claimName, Long.class);
            // 白名单校验：登出后的 token 即使未过期也不可用
            if (id == null || !tokenService.isValid(tokenScope, token)) {
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