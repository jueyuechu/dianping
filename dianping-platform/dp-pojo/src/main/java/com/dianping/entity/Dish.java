package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品表 tb_dish
 */
@Data
@TableName("tb_dish")
public class Dish {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 店铺 id */
    private Long shopId;

    /** 菜品分类 id（tb_dish_category） */
    private Long categoryId;

    /** 名称 */
    private String name;

    /** 图片 */
    private String image;

    /** 价格 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 描述 */
    private String description;

    /** 库存 */
    private Integer stock;

    /** 状态：1 上架 0 下架 2 售罄 3 停售 */
    private Integer status;

    /** 是否推荐 */
    private Integer isRecommend;

    /** 销量 */
    private Integer sold;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}