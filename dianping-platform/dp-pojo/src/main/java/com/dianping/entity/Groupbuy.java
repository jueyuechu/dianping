package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购表 tb_groupbuy
 */
@Data
@TableName("tb_groupbuy")
public class Groupbuy {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 店铺 id */
    private Long shopId;

    /** 团购名 */
    private String name;

    /** 图片 */
    private String image;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 团购价 */
    private BigDecimal groupbuyPrice;

    /** 内容明细 */
    private String content;

    /** 类型：1 套餐 2 代金券 3 折扣券 4 次卡 5 多人餐 */
    private Integer type;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sold;

    /** 状态：1 上架 0 下架 2 审核中 */
    private Integer status;

    /** 核销方式：1 扫码 2 输入核销码 3 两者均可 */
    private Integer verifyType;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}