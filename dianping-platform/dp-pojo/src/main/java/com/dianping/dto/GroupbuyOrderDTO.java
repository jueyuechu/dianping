package com.dianping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 团购下单 DTO
 */
@Data
public class GroupbuyOrderDTO {

    @NotNull(message = "团购 id 不能为空")
    private Long groupbuyId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;
}