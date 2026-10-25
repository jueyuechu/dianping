package com.dianping.controller.merchant;

import com.dianping.common.result.Result;
import com.dianping.common.utils.JwtUtil;
import com.dianping.dto.MerchantLoginDTO;
import com.dianping.dto.MerchantRegisterDTO;
import com.dianping.service.MerchantService;
import com.dianping.service.TokenService;
import com.dianping.vo.MerchantLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商户端认证接口
 */
@RestController
@RequestMapping("/api/merchant/auth")
public class MerchantAuthController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private TokenService tokenService;

    /**
     * 商户/员工登录
     */
    @PostMapping("/login")
    public Result<MerchantLoginVO> login(@RequestBody @Valid MerchantLoginDTO merchantLoginDTO) {
        return Result.success(merchantService.login(merchantLoginDTO));
    }

    /**
     * 商户入驻申请（提交后进入管理员审核队列）
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid MerchantRegisterDTO dto) {
        return Result.success(merchantService.register(dto));
    }

    /**
     * 入驻审核进度
     */
    @GetMapping("/apply-status")
    public Result<Map<String, Object>> applyStatus() {
        return Result.success(merchantService.applyStatus());
    }

    /**
     * 登出：从 Redis 白名单移除当前 token
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        tokenService.revoke(TokenService.SCOPE_MERCHANT, JwtUtil.resolveToken(request.getHeader("Authorization")));
        return Result.success();
    }
}