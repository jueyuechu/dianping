package com.dianping.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付回调参数（微信支付 V3 接入前的回调骨架）
 */
@Data
public class PayNotifyDTO {

    /** 商户订单号 */
    private String orderNo;

    /** 支付流水号 */
    private String payNo;

    private BigDecimal amount;

    /** 支付结果：SUCCESS / FAIL */
    private String tradeState;
}
