package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核记录表 tb_audit_record（全程留痕）
 */
@Data
@TableName("tb_audit_record")
public class AuditRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    /** 审核人 id（系统自动操作则为 NULL） */
    private Long auditorId;

    /** 动作：1 提交 2 接单 3 通过 4 驳回 5 上架 6 下架 7 封禁 8 释放 */
    private Integer action;

    /** 意见 / 驳回原因 */
    private String comment;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}