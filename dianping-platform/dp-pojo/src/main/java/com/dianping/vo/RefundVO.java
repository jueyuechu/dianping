package com.dianping.vo;

import com.dianping.entity.Refund;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款列表项（含订单号，便于用户/管理端展示）
 */
@Data
public class RefundVO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private BigDecimal amount;
    private String reason;
    private Integer status;
    private LocalDateTime applyTime;
    private LocalDateTime refundTime;

    public static RefundVO of(Refund refund, String orderNo) {
        RefundVO vo = new RefundVO();
        vo.setId(refund.getId());
        vo.setOrderId(refund.getOrderId());
        vo.setOrderNo(orderNo);
        vo.setAmount(refund.getAmount());
        vo.setReason(refund.getReason());
        vo.setStatus(refund.getStatus());
        vo.setApplyTime(refund.getApplyTime());
        vo.setRefundTime(refund.getRefundTime());
        return vo;
    }
}
