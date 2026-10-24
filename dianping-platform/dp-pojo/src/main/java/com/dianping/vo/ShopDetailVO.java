package com.dianping.vo;

import com.dianping.entity.Dish;
import com.dianping.entity.Groupbuy;
import com.dianping.entity.Shop;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 店铺详情 VO（门店信息 + 评分 + 菜品 + 团购）
 */
@Data
@Builder
public class ShopDetailVO {

    private Shop shop;

    private List<Dish> dishes;

    private List<Groupbuy> groupbuys;
}