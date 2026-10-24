package com.dianping.vo;

import com.dianping.entity.Groupbuy;
import com.dianping.entity.GroupbuyRule;
import lombok.Builder;
import lombok.Data;

/**
 * 团购详情 VO（团购 + 规则 + 店铺名）
 */
@Data
@Builder
public class GroupbuyDetailVO {

    private Groupbuy groupbuy;

    private GroupbuyRule rule;

    private Long shopId;

    private String shopName;
}