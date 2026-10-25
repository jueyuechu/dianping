package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息 tb_message：MQ 消费者落库（下单/核销/审核结果通知）
 */
@Data
@TableName("tb_message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户 id */
    private Long userId;

    /** 类型：1 系统 2 商家回复 3 评论 4 订阅 */
    private Integer type;

    private String content;

    /** 0 未读 1 已读 */
    private Integer isRead;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
