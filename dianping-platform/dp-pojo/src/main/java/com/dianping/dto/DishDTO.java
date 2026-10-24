package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户菜品新增/编辑 DTO
 */
@Data
public class DishDTO {

    @NotNull(message = "店铺 id 不能为空")
    private Long shopId;

    private Long categoryId;

    @NotBlank(message = "菜品名称不能为空")
    private String name;

    private String image;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private String description;

    private Integer stock;

    private Integer isRecommend;
}