package com.dianping.service;

import com.dianping.dto.UserLoginDTO;
import com.dianping.vo.UserLoginVO;

public interface UserService {

    /**
     * 微信登录（骨架阶段用 code 生成演示 openid，Phase 1 接入微信接口换真实 openid）
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);
}