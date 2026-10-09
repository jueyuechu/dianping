package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报表 tb_report
 */
@Data
@TableName("tb_report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 举报人 id */
    private Long reporterId;

    /** 举报对象：1 用户 2 评价 3 商家 4 帖子 */
    private Integer targetType;

    /** 对象 id */
    private Long targetId;

    /** 举报原因 */
    private String reason;

    /** 处理结果：1 驳回 2 删除内容 3 警告 4 封禁（NULL 表示待处理） */
    private Integer result;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}