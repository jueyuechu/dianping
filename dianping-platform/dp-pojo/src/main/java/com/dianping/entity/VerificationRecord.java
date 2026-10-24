package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 核销记录表 tb_verification_record
 */
@Data
@TableName("tb_verification_record")
public class VerificationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    /** 操作员 id（商户） */
    private Long operatorId;

    private LocalDateTime verifyTime;

    /** 核销方式：1 扫码 2 输入核销码 */
    private Integer method;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}