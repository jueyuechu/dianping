package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关注表 tb_follow（关注流使用）
 */
@Data
@TableName("tb_follow")
public class Follow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关注者 */
    private Long userId;

    /** 被关注者 */
    private Long followUserId;

    private LocalDateTime createTime;
}