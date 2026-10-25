package com.dianping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 核销完成通知（MQ 消息体）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVerifiedMessage {

    private Long orderId;

    private String orderNo;

    private String verifyCode;

    private Long userId;

    private Long shopId;

    private Long operatorId;

    /** yyyy-MM-dd HH:mm:ss */
    private String verifyTime;
}
