package com.dianping.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单支付 DTO
 */
@Data
public class PayDTO {

    @NotNull(message = "订单 id 不能为空")
    private Long orderId;
}