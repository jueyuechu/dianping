package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户微信登录 DTO
 */
@Data
public class UserLoginDTO {

    /** 微信临时凭证（正式环境由 code 换 openid） */
    @NotBlank(message = "code 不能为空")
    private String code;

    private String nickName;

    private String avatar;
}