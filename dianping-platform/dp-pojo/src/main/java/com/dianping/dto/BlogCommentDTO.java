package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 帖子评论 DTO（楼中楼：parentId/answerId 可选）
 */
@Data
public class BlogCommentDTO {

    @NotBlank(message = "评论内容不能为空")
    private String content;

    /** 顶级评论 id（回复楼中楼时传） */
    private Long parentId;

    /** 回复目标用户 id */
    private Long answerId;
}