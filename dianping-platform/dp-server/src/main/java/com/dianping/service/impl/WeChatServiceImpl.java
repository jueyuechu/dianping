package com.dianping.service.impl;

import com.dianping.common.exception.BaseException;
import com.dianping.common.properties.WeChatProperties;
import com.dianping.service.WeChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 微信 code2session 登录
 * 文档：https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
 */
@Slf4j
@Service
public class WeChatServiceImpl implements WeChatService {

    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session"
                    + "?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    @Autowired
    private WeChatProperties weChatProperties;

    private final RestClient restClient = RestClient.create();

    @Override
    public String code2Openid(String code) {
        if (weChatProperties.isMock()) {
            return "mock-openid-" + code;
        }
        try {
            Map<?, ?> resp = restClient.get()
                    .uri(CODE2SESSION_URL,
                            weChatProperties.getAppid(),
                            weChatProperties.getSecret(),
                            code)
                    .retrieve()
                    .body(Map.class);
            if (resp == null) {
                throw new BaseException("微信登录失败：无响应");
            }
            Object openid = resp.get("openid");
            if (openid == null) {
                Object errmsg = resp.get("errmsg");
                log.warn("微信 code2session 失败：{}", resp);
                throw new BaseException("微信登录失败：" + (errmsg == null ? "未知错误" : errmsg));
            }
            return openid.toString();
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信 code2session 异常", e);
            throw new BaseException("微信登录失败，请稍后再试");
        }
    }
}