package com.dianping.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 管理员看板总览 VO
 */
@Data
@Builder
public class DashboardVO {

    /** 用户总数 */
    private Long userCount;

    /** 商户总数 */
    private Long merchantCount;

    /** 店铺总数 */
    private Long shopCount;

    /** 评价总数 */
    private Long reviewCount;

    /** 订单总数 */
    private Long orderCount;

    /** 待审核任务数 */
    private Long pendingAuditCount;

    /** 待处理举报数 */
    private Long pendingReportCount;
}