package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价表 tb_review（评分字段均乘 10 存整数；order_id 为消费凭证）
 */
@Data
@TableName("tb_review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 id */
    private Long userId;

    /** 店铺 id */
    private Long shopId;

    /** 关联订单（消费凭证） */
    private Long orderId;

    /** 总分（乘10） */
    private Integer score;

    /** 口味（乘10） */
    private Integer tasteScore;

    /** 环境（乘10） */
    private Integer envScore;

    /** 服务（乘10） */
    private Integer serviceScore;

    /** 性价比（乘10） */
    private Integer valueScore;

    /** 文字评价 */
    private String content;

    /** 标签（逗号分隔：分量足/服务好...） */
    private String tags;

    /** 是否匿名 */
    private Integer isAnonymous;

    /** 是否追评 */
    private Integer isAppend;

    /** 追评的原始评价 id */
    private Long parentId;

    /** 状态：1 正常 0 隐藏 2 删除 */
    private Integer status;

    /** 点赞数 */
    private Integer likeCount;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}