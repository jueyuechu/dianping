package com.dianping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核结果通知（MQ 消息体）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditResultMessage {

    private Long taskId;

    /** 1 商户 2 店铺 3 菜品 4 团购 5 评价 6 图片 7 帖子 */
    private Integer bizType;

    private Long bizId;

    /** true 通过 false 驳回 */
    private Boolean passed;

    private String reason;

    private Long submitterId;

    /** yyyy-MM-dd HH:mm:ss */
    private String auditTime;
}
