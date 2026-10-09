package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户封禁表 tb_user_ban（分级封禁）
 */
@Data
@TableName("tb_user_ban")
public class UserBan {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 id */
    private Long userId;

    /** 封禁范围：1 禁登录 2 禁评价 3 禁评论 4 禁下单 5 禁私信 */
    private Integer banType;

    /** 封禁原因 */
    private String reason;

    private LocalDateTime startTime;

    /** 结束时间（永久则为 NULL） */
    private LocalDateTime endTime;

    /** 0 生效 1 解除 */
    private Integer status;

    /** 操作管理员 id */
    private Long operatorId;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}