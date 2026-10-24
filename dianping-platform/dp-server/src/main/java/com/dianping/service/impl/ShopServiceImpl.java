package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BaseException;
import com.dianping.entity.Dish;
import com.dianping.entity.Groupbuy;
import com.dianping.entity.Shop;
import com.dianping.mapper.DishMapper;
import com.dianping.mapper.GroupbuyMapper;
import com.dianping.mapper.ShopMapper;
import com.dianping.service.ShopService;
import com.dianping.vo.ShopDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private GroupbuyMapper groupbuyMapper;

    @Override
    public List<Shop> nearby() {
        // Phase 1：入参 x/y 经纬度 + 高德距离计算 + Redis 缓存 + 分类/评分/距离筛选
        return shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1)
                .orderByDesc(Shop::getScore)
                .last("LIMIT 10"));
    }

    @Override
    public ShopDetailVO detail(Long id) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            throw new BaseException(404, "店铺不存在");
        }
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getShopId, id)
                .eq(Dish::getStatus, 1)
                .orderByAsc(Dish::getCategoryId));
        List<Groupbuy> groupbuys = groupbuyMapper.selectList(new LambdaQueryWrapper<Groupbuy>()
                .eq(Groupbuy::getShopId, id)
                .eq(Groupbuy::getStatus, 1));
        return ShopDetailVO.builder()
                .shop(shop)
                .dishes(dishes)
                .groupbuys(groupbuys)
                .build();
    }
}