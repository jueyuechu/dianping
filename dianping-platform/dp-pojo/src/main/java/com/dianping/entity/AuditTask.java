package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一审核任务表 tb_audit_task（对齐《审核状态机详细设计.md》）
 */
@Data
@TableName("tb_audit_task")
public class AuditTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务类型：1 商户 2 店铺 3 菜品 4 团购 5 评价 6 图片 7 帖子 */
    private Integer bizType;

    /** 业务对象 id */
    private Long bizId;

    /** 状态：0 草稿 1 待审核 2 审核中 3 通过 4 驳回 5 上架 6 下架 7 封禁 */
    private Integer status;

    /** 提交人 id */
    private Long submitterId;

    /** 审核人 id（接单后写入） */
    private Long auditorId;

    /** 结果：1 通过 2 驳回 3 批量通过 */
    private Integer result;

    /** 驳回原因 */
    private String reason;

    private LocalDateTime createTime;

    /** 审核时间 */
    private LocalDateTime auditTime;

    @TableLogic
    private Integer deleted;
}