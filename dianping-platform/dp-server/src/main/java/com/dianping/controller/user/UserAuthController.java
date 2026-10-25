package com.dianping.controller.user;

import com.dianping.common.result.Result;
import com.dianping.common.utils.JwtUtil;
import com.dianping.dto.UserLoginDTO;
import com.dianping.service.TokenService;
import com.dianping.service.UserService;
import com.dianping.vo.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private TokenService tokenService;

    /**
     * 微信登录（code 换 openid + 签发 JWT）
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        return Result.success(userService.login(userLoginDTO));
    }

    /**
     * 登出：从 Redis 白名单移除当前 token
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        tokenService.revoke(TokenService.SCOPE_USER, JwtUtil.resolveToken(request.getHeader("Authorization")));
        return Result.success();
    }
}