package com.dianping.vo;

import com.dianping.entity.Dish;
import com.dianping.entity.DishSku;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 菜品详情 VO（含多规格）
 */
@Data
@Builder
public class DishDetailVO {

    private Dish dish;

    private List<DishSku> skus;
}