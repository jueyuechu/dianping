package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.dto.ReviewDTO;
import com.dianping.entity.GroupbuyOrder;
import com.dianping.entity.Review;
import com.dianping.entity.ReviewImage;
import com.dianping.entity.Shop;
import com.dianping.mapper.GroupbuyOrderMapper;
import com.dianping.mapper.ReviewImageMapper;
import com.dianping.mapper.ReviewMapper;
import com.dianping.mapper.ShopMapper;
import com.dianping.service.ReviewService;
import com.dianping.service.SensitiveWordService;
import com.dianping.service.UserBanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ReviewImageMapper reviewImageMapper;

    @Autowired
    private GroupbuyOrderMapper groupbuyOrderMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private UserBanService userBanService;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Review create(ReviewDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(401, "未登录或登录已过期");
        }
        // 禁评价封禁校验 + 敏感词过滤
        userBanService.assertAllowed(userId, 2);
        dto.setContent(sensitiveWordService.filter(dto.getContent()));
        // 消费凭证校验：订单属于当前用户，且已核销（状态 3）
        GroupbuyOrder order = groupbuyOrderMapper.selectById(dto.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BaseException(404, "订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 3) {
            throw new BaseException("订单未核销，暂不能评价");
        }
        Long exists = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId()));
        if (exists != null && exists > 0) {
            throw new BaseException("该订单已评价");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setShopId(dto.getShopId());
        review.setOrderId(dto.getOrderId());
        review.setScore(dto.getScore());
        review.setTasteScore(dto.getTasteScore());
        review.setEnvScore(dto.getEnvScore());
        review.setServiceScore(dto.getServiceScore());
        review.setValueScore(dto.getValueScore());
        review.setContent(dto.getContent());
        review.setTags(dto.getTags() == null ? null : String.join(",", dto.getTags()));
        review.setIsAnonymous(dto.getIsAnonymous() == null ? 0 : dto.getIsAnonymous());
        review.setIsAppend(0);
        // 评价先发后审：默认正常展示，敏感词与举报由审核/风控环节处理
        review.setStatus(1);
        review.setLikeCount(0);
        reviewMapper.insert(review);

        if (dto.getImages() != null) {
            for (String url : dto.getImages()) {
                ReviewImage image = new ReviewImage();
                image.setReviewId(review.getId());
                image.setUrl(url);
                image.setType(1);
                reviewImageMapper.insert(image);
            }
        }

        // 店铺评论数 +1（评分聚合在 Phase 1 由定时任务/异步重算）
        shopMapper.update(null, new LambdaUpdateWrapper<Shop>()
                .eq(Shop::getId, dto.getShopId())
                .setSql("comments = comments + 1"));
        return review;
    }

    @Override
    public List<Review> my() {
        Long userId = BaseContext.getCurrentId();
        return reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime));
    }
}