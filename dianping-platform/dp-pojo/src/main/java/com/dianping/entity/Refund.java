package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款表 tb_refund
 */
@Data
@TableName("tb_refund")
public class Refund {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String refundNo;

    private BigDecimal amount;

    private String reason;

    /** 1 申请 2 审核 3 退款中 4 已退 5 驳回 */
    private Integer status;

    private LocalDateTime applyTime;

    private LocalDateTime refundTime;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
