package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 数据助手提问 DTO
 */
@Data
public class AiQueryDTO {

    @NotBlank(message = "问题不能为空")
    private String question;
}