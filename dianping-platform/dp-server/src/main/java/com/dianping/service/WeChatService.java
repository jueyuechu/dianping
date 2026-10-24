package com.dianping.service;

public interface WeChatService {

    /**
     * 微信登录：code 换取 openid
     * mock 模式直接返回 mock-openid-{code}，便于本地联调
     */
    String code2Openid(String code);
}