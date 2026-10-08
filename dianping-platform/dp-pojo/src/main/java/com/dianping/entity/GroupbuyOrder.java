package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购订单表 tb_groupbuy_order
 */
@Data
@TableName("tb_groupbuy_order")
public class GroupbuyOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 用户 id */
    private Long userId;

    /** 团购 id */
    private Long groupbuyId;

    /** 店铺 id */
    private Long shopId;

    /** 数量 */
    private Integer quantity;

    /** 金额 */
    private BigDecimal amount;

    /** 状态：1 待支付 2 待使用 3 已使用 4 已退款 5 已过期 */
    private Integer status;

    /** 核销码 */
    private String verifyCode;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}