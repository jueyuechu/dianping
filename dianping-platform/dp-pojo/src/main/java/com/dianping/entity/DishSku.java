package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品规格表 tb_dish_sku（多规格：大份/小份、辣度、加料）
 */
@Data
@TableName("tb_dish_sku")
public class DishSku {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dishId;

    /** 规格名 */
    private String specName;

    /** 规格值 */
    private String specValue;

    private BigDecimal price;

    private Integer stock;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}