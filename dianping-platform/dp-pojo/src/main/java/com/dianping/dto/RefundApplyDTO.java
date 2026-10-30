package com.dianping.dto;

import lombok.Data;

/**
 * 退款申请
 */
@Data
public class RefundApplyDTO {

    private Long orderId;

    private String reason;
}
