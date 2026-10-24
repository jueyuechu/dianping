package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 核销 DTO
 */
@Data
public class VerifyDTO {

    @NotBlank(message = "核销码不能为空")
    private String verifyCode;

    /** 核销方式：1 扫码 2 输入核销码（默认 2） */
    private Integer method;
}