package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子评论表 tb_blog_comment（楼中楼）
 */
@Data
@TableName("tb_blog_comment")
public class BlogComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long blogId;

    private Long userId;

    /** 顶级评论 id（楼中楼，顶评为 NULL） */
    private Long parentId;

    /** 回复目标用户 id */
    private Long answerId;

    private String content;

    /** 状态：0 正常 1 删除 2 隐藏 */
    private Integer status;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}