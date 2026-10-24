package com.dianping.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子评论 VO（含评论人昵称）
 */
@Data
public class BlogCommentVO {

    private Long id;

    private Long userId;

    private String userName;

    private Long parentId;

    private Long answerId;

    private String answerName;

    private String content;

    private LocalDateTime createTime;
}