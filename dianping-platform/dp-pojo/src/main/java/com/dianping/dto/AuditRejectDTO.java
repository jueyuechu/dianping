package com.dianping.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核驳回 DTO
 */
@Data
public class AuditRejectDTO {

    @NotNull(message = "驳回原因不能为空")
    private String reason;

    /** 驳回原因模板 id（可选） */
    private Long templateId;
}