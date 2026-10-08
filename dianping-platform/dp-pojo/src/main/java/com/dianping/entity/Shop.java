package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺表 tb_shop（评分字段均乘 10 存整数，如 4.9 分存 49）
 */
@Data
@TableName("tb_shop")
public class Shop {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商户 id */
    private Long merchantId;

    /** 店铺名 */
    private String name;

    /** 品类 id（对应 tb_category） */
    private Long typeId;

    /** 图片，逗号分隔 */
    private String images;

    /** 商圈 */
    private String area;

    /** 地址 */
    private String address;

    /** 经度 */
    private Double x;

    /** 纬度 */
    private Double y;

    /** 人均（元） */
    private Long avgPrice;

    /** 销量 */
    private Integer sold;

    /** 评论数 */
    private Integer comments;

    /** 综合评分（乘10） */
    private Integer score;

    /** 口味评分（乘10） */
    private Integer tasteScore;

    /** 环境评分（乘10） */
    private Integer envScore;

    /** 服务评分（乘10） */
    private Integer serviceScore;

    /** 性价比评分（乘10） */
    private Integer valueScore;

    /** 营业时间 */
    private String openHours;

    /** 电话 */
    private String phone;

    /** 营业状态：1 营业 0 休息 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}