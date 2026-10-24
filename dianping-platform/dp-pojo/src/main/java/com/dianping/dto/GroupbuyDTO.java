package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户团购新增/编辑 DTO（含使用与退款规则）
 */
@Data
public class GroupbuyDTO {

    @NotNull(message = "店铺 id 不能为空")
    private Long shopId;

    @NotBlank(message = "团购名称不能为空")
    private String name;

    private String image;

    private BigDecimal originalPrice;

    @NotNull(message = "团购价不能为空")
    private BigDecimal groupbuyPrice;

    private String content;

    /** 类型：1 套餐 2 代金券 3 折扣券 4 次卡 5 多人餐 */
    private Integer type;

    private Integer stock;

    /** 核销方式：1 扫码 2 输入核销码 3 两者均可 */
    private Integer verifyType;

    /** 节假日是否可用 */
    private Integer holidayUsable;

    /** 每人限购（0 不限） */
    private Integer perUserLimit;

    /** 每桌限用 */
    private Integer perTableLimit;

    private LocalDateTime validStart;

    private LocalDateTime validEnd;

    /** 退款规则：1 随时退 2 过期退 3 不可退 */
    private Integer refundRule;
}