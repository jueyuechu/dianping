package com.dianping.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户封禁 DTO（分级封禁 + 时长）
 */
@Data
public class UserBanDTO {

    /** 封禁范围：1 禁登录 2 禁评价 3 禁评论 4 禁下单 5 禁私信 */
    @NotNull(message = "封禁类型不能为空")
    private Integer banType;

    private String reason;

    /** 封禁天数，0 表示永久 */
    private Integer duration;
}