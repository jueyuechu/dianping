package com.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词表 tb_sensitive_word
 * level：1 低危（打码） 2 中危（打码 + 人工复审） 3 高危（直接拦截）
 */
@Data
@TableName("tb_sensitive_word")
public class SensitiveWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String word;

    private Integer level;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
