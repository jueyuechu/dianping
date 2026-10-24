package com.dianping.service;

import com.dianping.dto.ReviewDTO;
import com.dianping.entity.Review;

import java.util.List;

public interface ReviewService {

    /**
     * 发布评价（校验消费凭证：订单须属于当前用户且已核销）
     */
    Review create(ReviewDTO dto);

    /**
     * 我的评价
     */
    List<Review> my();
}