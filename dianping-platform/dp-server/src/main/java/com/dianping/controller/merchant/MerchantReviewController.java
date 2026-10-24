package com.dianping.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.common.result.Result;
import com.dianping.dto.ReplyDTO;
import com.dianping.entity.Review;
import com.dianping.entity.ReviewReply;
import com.dianping.entity.Shop;
import com.dianping.mapper.ReviewMapper;
import com.dianping.mapper.ReviewReplyMapper;
import com.dianping.mapper.ShopMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商户端评价管理：查看评价、商家回复
 */
@RestController
@RequestMapping("/api/merchant/review")
public class MerchantReviewController {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ReviewReplyMapper reviewReplyMapper;

    @Autowired
    private ShopMapper shopMapper;

    /**
     * 我的店铺评价列表（可按店铺筛选）
     */
    @GetMapping("/list")
    public Result<Page<Review>> list(@RequestParam(defaultValue = "1") long current,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) Long shopId,
                                     @RequestParam(required = false) Integer status) {
        List<Long> shopIds = currentShopIds();
        Page<Review> page = new Page<>(current, size);
        if (shopIds.isEmpty()) {
            return Result.success(page);
        }
        if (shopId != null) {
            requireOwnedShop(shopId);
            shopIds = List.of(shopId);
        }
        return Result.success(reviewMapper.selectPage(page, new LambdaQueryWrapper<Review>()
                .in(Review::getShopId, shopIds)
                .eq(status != null, Review::getStatus, status)
                .orderByDesc(Review::getCreateTime)));
    }

    /**
     * 商家回复评价
     */
    @PostMapping("/{id}/reply")
    public Result<ReviewReply> reply(@PathVariable Long id, @RequestBody @Valid ReplyDTO dto) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BaseException(404, "评价不存在");
        }
        requireOwnedShop(review.getShopId());

        ReviewReply reply = new ReviewReply();
        reply.setReviewId(id);
        reply.setUserId(BaseContext.getCurrentId());
        reply.setContent(dto.getContent());
        reply.setType(1);
        reviewReplyMapper.insert(reply);
        return Result.success(reply);
    }

    /**
     * 评价的回复列表
     */
    @GetMapping("/{id}/replies")
    public Result<List<ReviewReply>> replies(@PathVariable Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BaseException(404, "评价不存在");
        }
        requireOwnedShop(review.getShopId());
        return Result.success(reviewReplyMapper.selectList(new LambdaQueryWrapper<ReviewReply>()
                .eq(ReviewReply::getReviewId, id)
                .orderByAsc(ReviewReply::getCreateTime)));
    }

    private List<Long> currentShopIds() {
        Long merchantId = BaseContext.getCurrentId();
        if (merchantId == null) {
            throw new BaseException(401, "未登录或登录已过期");
        }
        return shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getMerchantId, merchantId))
                .stream().map(Shop::getId).collect(Collectors.toList());
    }

    private void requireOwnedShop(Long shopId) {
        if (!currentShopIds().contains(shopId)) {
            throw new BaseException(403, "无权操作该店铺评价");
        }
    }
}