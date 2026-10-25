package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.constant.JwtClaimsConstant;
import com.dianping.common.constant.MessageConstant;
import com.dianping.common.exception.BaseException;
import com.dianping.common.properties.JwtProperties;
import com.dianping.common.utils.JwtUtil;
import com.dianping.common.utils.PasswordUtil;
import com.dianping.dto.AdminLoginDTO;
import com.dianping.entity.Admin;
import com.dianping.mapper.AdminMapper;
import com.dianping.service.AdminService;
import com.dianping.service.TokenService;
import com.dianping.vo.AdminLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private TokenService tokenService;

    @Override
    public AdminLoginVO login(AdminLoginDTO adminLoginDTO) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, adminLoginDTO.getUsername()));
        if (admin == null || !PasswordUtil.matches(adminLoginDTO.getPassword(), admin.getPassword())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }
        // 历史明文密码在首次登录成功后自动升级为 BCrypt
        if (!PasswordUtil.isEncoded(admin.getPassword())) {
            admin.setPassword(PasswordUtil.encode(adminLoginDTO.getPassword()));
            adminMapper.updateById(admin);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);
        tokenService.save(TokenService.SCOPE_ADMIN, token, jwtProperties.getAdminTtl());

        return AdminLoginVO.builder()
                .id(admin.getId())
                .token(token)
                .username(admin.getUsername())
                .realName(admin.getRealName())
                .build();
    }
}