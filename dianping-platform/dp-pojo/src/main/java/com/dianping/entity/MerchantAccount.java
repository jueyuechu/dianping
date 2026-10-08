package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商户员工账号表 tb_merchant_account（商户端登录凭据）
 */
@Data
@TableName("tb_merchant_account")
public class MerchantAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商户 id */
    private Long merchantId;

    /** 员工账号 */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 角色：1 老板 2 店长 3 员工 */
    private Integer role;

    /** 状态：1 正常 0 禁用 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}