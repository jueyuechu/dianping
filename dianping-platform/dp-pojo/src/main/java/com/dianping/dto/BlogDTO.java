package com.dianping.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 发帖 DTO
 */
@Data
public class BlogDTO {

    /** 关联店铺（type=1 探店笔记时必填） */
    private Long shopId;

    /** 类型：1 探店笔记 2 自由动态 */
    private Integer type;

    private String title;

    @NotBlank(message = "正文不能为空")
    private String content;

    private List<String> images;

    private String location;
}