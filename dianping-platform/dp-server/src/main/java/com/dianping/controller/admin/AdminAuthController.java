package com.dianping.controller.admin;

import com.dianping.common.result.Result;
import com.dianping.dto.AdminLoginDTO;
import com.dianping.service.AdminService;
import com.dianping.vo.AdminLoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
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

    /**
     * 管理员登录（Phase 1 加验证码与二次验证）
     */
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@RequestBody @Valid AdminLoginDTO adminLoginDTO) {
        return Result.success(adminService.login(adminLoginDTO));
    }
}