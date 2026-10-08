package com.dianping.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户登录返回 VO
 */
@Data
@Builder
public class UserLoginVO {

    private Long id;

    private String token;

    private String nickName;

    private String icon;
}