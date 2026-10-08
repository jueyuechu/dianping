package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类表 tb_category（一级/二级：parent_id 区分层级，店铺品类 type_id 亦引用该表）
 */
@Data
@TableName("tb_category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名：美食/火锅/烧烤/奶茶/酒店 */
    private String name;

    /** 父分类 id（0 表示一级） */
    private Long parentId;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
}