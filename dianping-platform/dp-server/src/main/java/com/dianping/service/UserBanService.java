package com.dianping.service;

/**
 * 用户封禁校验
 */
public interface UserBanService {

    /**
     * 校验用户是否存在生效中的封禁，存在则抛出业务异常
     *
     * @param banType 1 禁登录 2 禁评价 3 禁评论 4 禁下单 5 禁私信
     */
    void assertAllowed(Long userId, int banType);
}
