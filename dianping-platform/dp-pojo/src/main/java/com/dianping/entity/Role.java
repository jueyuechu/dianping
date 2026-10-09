package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色表 tb_role
 */
@Data
@TableName("tb_role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名：超级管理员/运营/审核员/财务/客服 */
    private String roleName;

    /** 角色标识 */
    private String roleKey;

    private Integer sort;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}