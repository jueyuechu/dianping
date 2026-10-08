package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商户/员工登录 DTO
 */
@Data
public class MerchantLoginDTO {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}