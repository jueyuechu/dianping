package com.dianping.config;

import com.dianping.common.constant.JwtClaimsConstant;
import com.dianping.common.properties.JwtProperties;
import com.dianping.interceptor.JwtTokenInterceptor;
import com.dianping.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置：注册三端 JWT 登录拦截器 + 本地上传文件静态访问
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private TokenService tokenService;

    @Value("${dianping.upload.local-path:./uploads}")
    private String uploadLocalPath;

    /**
     * 本地上传文件静态映射：/uploads/** → 本地目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolute = Paths.get(uploadLocalPath).toAbsolutePath().normalize().toString() + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolute);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户端：登录/登出放行；浏览类 GET（店铺/菜品/团购详情）可匿名，下单/评价等写操作需登录
        registry.addInterceptor(new JwtTokenInterceptor(
                        jwtProperties.getUserSecretKey(),
                        JwtClaimsConstant.USER_ID,
                        TokenService.SCOPE_USER,
                        tokenService,
                        java.util.List.of("/api/user/groupbuy/", "/api/user/blog/")))
                .addPathPatterns("/api/user/**")
                .excludePathPatterns(
                        "/api/user/auth/login",
                        "/api/user/auth/logout",
                        "/api/user/shop/**",
                        "/api/user/dish/**");

        // 管理员端：仅登录/登出放行
        registry.addInterceptor(new JwtTokenInterceptor(
                        jwtProperties.getAdminSecretKey(),
                        JwtClaimsConstant.ADMIN_ID,
                        TokenService.SCOPE_ADMIN,
                        tokenService,
                        java.util.List.of()))
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login", "/api/admin/auth/logout");

        // 商户端：登录/登出与入驻申请放行
        registry.addInterceptor(new JwtTokenInterceptor(
                        jwtProperties.getMerchantSecretKey(),
                        JwtClaimsConstant.MERCHANT_ID,
                        TokenService.SCOPE_MERCHANT,
                        tokenService,
                        java.util.List.of()))
                .addPathPatterns("/api/merchant/**")
                .excludePathPatterns(
                        "/api/merchant/auth/login",
                        "/api/merchant/auth/logout",
                        "/api/merchant/auth/register");
    }
}