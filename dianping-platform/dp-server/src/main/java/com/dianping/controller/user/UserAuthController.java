package com.dianping.controller.user;

import com.dianping.common.result.Result;
import com.dianping.dto.UserLoginDTO;
import com.dianping.service.UserService;
import com.dianping.vo.UserLoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端认证接口
 */
@RestController
@RequestMapping("/api/user/auth")
public class UserAuthController {

    @Autowired
    private UserService userService;

    /**
     * 微信登录（code 换 openid + 签发 JWT）
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        return Result.success(userService.login(userLoginDTO));
    }
}