package com.dianping.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.result.Result;
import com.dianping.entity.Dish;
import com.dianping.entity.Groupbuy;
import com.dianping.entity.Review;
import com.dianping.mapper.DishMapper;
import com.dianping.mapper.GroupbuyMapper;
import com.dianping.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端内容管理：菜品/团购/评价的下架、删除、隐藏
 */
@RestController
@RequestMapping("/api/admin")
public class AdminContentController {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private GroupbuyMapper groupbuyMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    // ---------- 菜品 ----------

    @GetMapping("/dish/list")
    public Result<Page<Dish>> dishList(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size) {
        return Result.success(dishMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Dish>().orderByDesc(Dish::getCreateTime)));
    }

    /**
     * 菜品状态：1 上架 0 下架（删除走逻辑删除）
     */
    @PutMapping("/dish/{id}/status")
    public Result<Void> dishStatus(@PathVariable Long id, @RequestParam Integer status) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            return Result.error(404, "菜品不存在");
        }
        dish.setStatus(status);
        dishMapper.updateById(dish);
        return Result.success();
    }

    @DeleteMapping("/dish/{id}")
    public Result<Void> dishDelete(@PathVariable Long id) {
        dishMapper.deleteById(id);
        return Result.success();
    }

    // ---------- 团购 ----------

    @GetMapping("/groupbuy/list")
    public Result<Page<Groupbuy>> groupbuyList(@RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "10") long size) {
        return Result.success(groupbuyMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Groupbuy>().orderByDesc(Groupbuy::getCreateTime)));
    }

    @PutMapping("/groupbuy/{id}/status")
    public Result<Void> groupbuyStatus(@PathVariable Long id, @RequestParam Integer status) {
        Groupbuy groupbuy = groupbuyMapper.selectById(id);
        if (groupbuy == null) {
            return Result.error(404, "团购不存在");
        }
        groupbuy.setStatus(status);
        groupbuyMapper.updateById(groupbuy);
        return Result.success();
    }

    @DeleteMapping("/groupbuy/{id}")
    public Result<Void> groupbuyDelete(@PathVariable Long id) {
        groupbuyMapper.deleteById(id);
        return Result.success();
    }

    // ---------- 评价 ----------

    @GetMapping("/review/list")
    public Result<Page<Review>> reviewList(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) Long shopId) {
        return Result.success(reviewMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Review>()
                        .eq(shopId != null, Review::getShopId, shopId)
                        .orderByDesc(Review::getCreateTime)));
    }

    /**
     * 评价状态：1 正常 0 隐藏 2 删除
     */
    @PutMapping("/review/{id}/status")
    public Result<Void> reviewStatus(@PathVariable Long id, @RequestParam Integer status) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            return Result.error(404, "评价不存在");
        }
        review.setStatus(status);
        reviewMapper.updateById(review);
        return Result.success();
    }

    @DeleteMapping("/review/{id}")
    public Result<Void> reviewDelete(@PathVariable Long id) {
        reviewMapper.deleteById(id);
        return Result.success();
    }
}