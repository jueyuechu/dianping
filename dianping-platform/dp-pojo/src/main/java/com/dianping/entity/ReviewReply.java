package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价回复表 tb_review_reply（商家回复 / 用户回复）
 */
@Data
@TableName("tb_review_reply")
public class ReviewReply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reviewId;

    /** 回复人 id（商家为 merchantId，用户为 userId） */
    private Long userId;

    private String content;

    /** 类型：1 商家回复 2 用户回复 */
    private Integer type;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}