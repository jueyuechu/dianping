package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子表 tb_blog（探店笔记 / 自由动态，shop_id 可空）
 */
@Data
@TableName("tb_blog")
public class Blog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 关联店铺（探店笔记必填，自由动态为 NULL） */
    private Long shopId;

    /** 类型：1 探店笔记 2 自由动态 */
    private Integer type;

    private String title;

    private String content;

    /** 图片/视频 URL，逗号分隔 */
    private String images;

    /** 定位（城市/商圈） */
    private String location;

    /** 状态：0 正常 1 隐藏 2 删除 */
    private Integer status;

    /** 点赞数（冗余） */
    private Integer liked;

    /** 评论数（冗余） */
    private Integer comments;

    /** 收藏数（冗余） */
    private Integer collected;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}