package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 团购规则表 tb_groupbuy_rule
 */
@Data
@TableName("tb_groupbuy_rule")
public class GroupbuyRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupbuyId;

    /** 节假日是否可用：1 可用 0 不可用 */
    private Integer holidayUsable;

    /** 每人限购（0 不限） */
    private Integer perUserLimit;

    /** 每桌限用 */
    private Integer perTableLimit;

    private LocalDateTime validStart;

    private LocalDateTime validEnd;

    /** 退款规则：1 随时退 2 过期退 3 不可退 */
    private Integer refundRule;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}