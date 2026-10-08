package com.dianping.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 商户/员工登录返回 VO
 */
@Data
@Builder
public class MerchantLoginVO {

    private Long id;

    private String token;

    private String username;

    private Long merchantId;

    /** 角色：1 老板 2 店长 3 员工 */
    private Integer role;

    /** 所属店铺名 */
    private String shopName;
}