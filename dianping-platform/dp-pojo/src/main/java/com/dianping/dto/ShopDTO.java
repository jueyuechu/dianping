package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商户店铺新增/编辑 DTO
 */
@Data
public class ShopDTO {

    @NotBlank(message = "店铺名不能为空")
    private String name;

    private Long typeId;

    private String images;

    private String area;

    private String address;

    /** 经度 */
    private Double x;

    /** 纬度 */
    private Double y;

    private Long avgPrice;

    private String openHours;

    private String phone;
}