package com.dianping.service;

import com.dianping.entity.Shop;
import com.dianping.vo.ShopDetailVO;

import java.util.List;

public interface ShopService {

    /**
     * 附近商家列表（骨架阶段演示：按评分倒序取前 10；Phase 1 加高德距离计算 + Redis 缓存）
     */
    List<Shop> nearby();

    /**
     * 店铺详情（门店信息 + 菜品 + 团购）
     */
    ShopDetailVO detail(Long id);
}