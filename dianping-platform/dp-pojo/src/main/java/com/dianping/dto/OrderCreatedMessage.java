package com.dianping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 下单成功通知（MQ 消息体）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedMessage {

    private Long orderId;

    private String orderNo;

    private Long userId;

    private Long shopId;

    private BigDecimal amount;

    /** yyyy-MM-dd HH:mm:ss */
    private String createTime;
}
