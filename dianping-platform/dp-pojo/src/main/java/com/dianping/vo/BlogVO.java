package com.dianping.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子列表/详情 VO（作者、店铺、当前用户点赞态）
 */
@Data
public class BlogVO {

    private Long id;

    private Long userId;

    private String authorName;

    private String authorIcon;

    private Long shopId;

    private String shopName;

    private Integer type;

    private String title;

    private String content;

    private List<String> images;

    private String location;

    /** 状态：0 正常 1 隐藏 2 删除 */
    private Integer status;

    private Integer likedCount;

    private Integer commentCount;

    private Integer collectCount;

    /** 当前登录用户是否已点赞 */
    private Boolean liked;

    private LocalDateTime createTime;
}