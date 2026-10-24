package com.dianping.controller.user;

import com.dianping.common.result.Result;
import com.dianping.service.ShopService;
import com.dianping.vo.ShopDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.dianping.entity.Shop;

/**
 * 用户端店铺接口（浏览类，暂不强制登录）
 */
@RestController
@RequestMapping("/api/user/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 附近商家（骨架演示；Phase 1 支持 x/y 距离排序、分类筛选、Redis 缓存）
     */
    @GetMapping("/nearby")
    public Result<List<Shop>> nearby() {
        return Result.success(shopService.nearby());
    }

    /**
     * 店铺详情：门店信息 + 评分 + 菜品 + 团购
     */
    @GetMapping("/{id}")
    public Result<ShopDetailVO> detail(@PathVariable Long id) {
        return Result.success(shopService.detail(id));
    }
}