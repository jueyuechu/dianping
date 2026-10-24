package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子点赞表 tb_blog_like（联合唯一索引防重复）
 */
@Data
@TableName("tb_blog_like")
public class BlogLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long blogId;

    private Long userId;

    private LocalDateTime createTime;
}