package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商户入驻申请 DTO（营业执照 + 法人 + 门店信息 + 登录账号）
 */
@Data
public class MerchantRegisterDTO {

    @NotBlank(message = "商户名称不能为空")
    private String name;

    @NotBlank(message = "营业执照号不能为空")
    private String licenseNo;

    private String licenseImg;

    @NotBlank(message = "法人不能为空")
    private String legalPerson;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    /** 登录账号 */
    @NotBlank(message = "登录账号不能为空")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    private String password;
}