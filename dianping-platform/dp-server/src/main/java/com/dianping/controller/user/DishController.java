package com.dianping.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.result.Result;
import com.dianping.entity.Dish;
import com.dianping.entity.DishSku;
import com.dianping.mapper.DishMapper;
import com.dianping.mapper.DishSkuMapper;
import com.dianping.vo.DishDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端菜品接口（浏览类，暂不强制登录）
 */
@RestController
@RequestMapping("/api/user/dish")
public class DishController {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishSkuMapper dishSkuMapper;

    /**
     * 菜品列表（按店铺，仅上架）
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(@RequestParam Long shopId) {
        return Result.success(dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getShopId, shopId)
                .eq(Dish::getStatus, 1)
                .orderByAsc(Dish::getCategoryId)));
    }

    /**
     * 菜品详情（含多规格）
     */
    @GetMapping("/{id}")
    public Result<DishDetailVO> detail(@PathVariable Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            return Result.error(404, "菜品不存在");
        }
        List<DishSku> skus = dishSkuMapper.selectList(new LambdaQueryWrapper<DishSku>()
                .eq(DishSku::getDishId, id));
        return Result.success(DishDetailVO.builder().dish(dish).skus(skus).build());
    }
}