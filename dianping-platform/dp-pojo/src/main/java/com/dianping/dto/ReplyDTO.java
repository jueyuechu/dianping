package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商家回复评价 DTO
 */
@Data
public class ReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    private String content;
}