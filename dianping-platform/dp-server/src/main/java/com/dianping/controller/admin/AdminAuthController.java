package com.dianping.controller.admin;

import com.dianping.common.result.Result;
import com.dianping.common.utils.JwtUtil;
import com.dianping.dto.AdminLoginDTO;
import com.dianping.service.AdminService;
import com.dianping.service.TokenService;
import com.dianping.vo.AdminLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端认证接口
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TokenService tokenService;

    /**
     * 管理员登录（Phase 1 加验证码与二次验证）
     */
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@RequestBody @Valid AdminLoginDTO adminLoginDTO) {
        return Result.success(adminService.login(adminLoginDTO));
    }

    /**
     * 登出：从 Redis 白名单移除当前 token
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        tokenService.revoke(TokenService.SCOPE_ADMIN, JwtUtil.resolveToken(request.getHeader("Authorization")));
        return Result.success();
    }
}