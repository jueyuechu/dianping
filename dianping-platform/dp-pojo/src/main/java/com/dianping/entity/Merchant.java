package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商户表 tb_merchant
 */
@Data
@TableName("tb_merchant")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商户名称 */
    private String name;

    /** 营业执照号 */
    private String licenseNo;

    /** 营业执照照片 */
    private String licenseImg;

    /** 法人 */
    private String legalPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 状态：0 待审核 1 正常 2 冻结 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}