package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付表 tb_payment
 */
@Data
@TableName("tb_payment")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    /** 支付流水号 */
    private String payNo;

    private BigDecimal amount;

    /** 支付渠道：1 微信支付 */
    private Integer channel;

    /** 状态：1 待支付 2 成功 3 失败 */
    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}