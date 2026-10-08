package com.dianping.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 发布评价 DTO（评分字段乘 10 存整数）
 */
@Data
public class ReviewDTO {

    /** 关联订单（消费凭证） */
    @NotNull(message = "订单 id 不能为空")
    private Long orderId;

    @NotNull(message = "店铺 id 不能为空")
    private Long shopId;

    /** 总分（乘10） */
    @NotNull(message = "评分不能为空")
    private Integer score;

    private Integer tasteScore;

    private Integer envScore;

    private Integer serviceScore;

    private Integer valueScore;

    private String content;

    /** 标签：["分量足","服务好"] */
    private List<String> tags;

    /** 图片 URL 列表 */
    private List<String> images;

    /** 是否匿名 */
    private Integer isAnonymous;
}