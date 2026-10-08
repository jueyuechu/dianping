package com.dianping.config;

import com.dianping.common.constant.JwtClaimsConstant;
import com.dianping.common.properties.JwtProperties;
import com.dianping.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册三端 JWT 登录拦截器
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户端：登录放行；浏览类接口（店铺/菜品/团购展示）暂不强制登录，下单/评价等需登录
        registry.addInterceptor(new JwtTokenInterceptor(jwtProperties.getUserSecretKey(), JwtClaimsConstant.USER_ID))
                .addPathPatterns("/api/user/**")
                .excludePathPatterns(
                        "/api/user/auth/login",
                        "/api/user/shop/**",
                        "/api/user/dish/**",
                        "/api/user/groupbuy/**");

        // 管理员端：仅登录放行
        registry.addInterceptor(new JwtTokenInterceptor(jwtProperties.getAdminSecretKey(), JwtClaimsConstant.ADMIN_ID))
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");

        // 商户端：登录与入驻申请放行
        registry.addInterceptor(new JwtTokenInterceptor(jwtProperties.getMerchantSecretKey(), JwtClaimsConstant.MERCHANT_ID))
                .addPathPatterns("/api/merchant/**")
                .excludePathPatterns("/api/merchant/auth/login", "/api/merchant/auth/register");
    }
}