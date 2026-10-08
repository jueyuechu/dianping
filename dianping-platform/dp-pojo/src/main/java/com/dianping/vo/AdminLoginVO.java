package com.dianping.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 管理员登录返回 VO
 */
@Data
@Builder
public class AdminLoginVO {

    private Long id;

    private String token;

    private String username;

    private String realName;
}