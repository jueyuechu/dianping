package com.dianping.controller.user;

import com.dianping.common.result.Result;
import com.dianping.dto.ReviewDTO;
import com.dianping.entity.Review;
import com.dianping.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端评价接口（需登录）
 */
@RestController
@RequestMapping("/api/user/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 发布评价（关联订单作为消费凭证，需订单已核销）
     */
    @PostMapping
    public Result<Review> create(@RequestBody @Valid ReviewDTO dto) {
        return Result.success(reviewService.create(dto));
    }

    /**
     * 我的评价
     */
    @GetMapping("/my")
    public Result<List<Review>> my() {
        return Result.success(reviewService.my());
    }
}