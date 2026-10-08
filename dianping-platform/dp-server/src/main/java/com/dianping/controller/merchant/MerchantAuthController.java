package com.dianping.controller.merchant;

import com.dianping.common.result.Result;
import com.dianping.dto.MerchantLoginDTO;
import com.dianping.service.MerchantService;
import com.dianping.vo.MerchantLoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户端认证接口
 */
@RestController
@RequestMapping("/api/merchant/auth")
public class MerchantAuthController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 商户/员工登录
     */
    @PostMapping("/login")
    public Result<MerchantLoginVO> login(@RequestBody @Valid MerchantLoginDTO merchantLoginDTO) {
        return Result.success(merchantService.login(merchantLoginDTO));
    }
}