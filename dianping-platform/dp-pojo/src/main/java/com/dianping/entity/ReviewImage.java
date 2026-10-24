package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价图片表 tb_review_image
 */
@Data
@TableName("tb_review_image")
public class ReviewImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reviewId;

    private String url;

    /** 类型：1 图片 2 视频 */
    private Integer type;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}