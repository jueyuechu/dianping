package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜品分类表 tb_dish_category
 */
@Data
@TableName("tb_dish_category")
public class DishCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;

    /** 分类名：热菜/凉菜/饮品/套餐 */
    private String name;

    private Integer sort;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}