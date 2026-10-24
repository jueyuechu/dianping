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

    /** 商户状态：0 待审核 1 正常 2 冻结（待审核可登录查看进度，但不可经营） */
    private Integer merchantStatus;
}