package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.constant.JwtClaimsConstant;
import com.dianping.common.constant.MessageConstant;
import com.dianping.common.exception.BaseException;
import com.dianping.common.properties.JwtProperties;
import com.dianping.common.utils.JwtUtil;
import com.dianping.dto.AdminLoginDTO;
import com.dianping.entity.Admin;
import com.dianping.mapper.AdminMapper;
import com.dianping.service.AdminService;
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

    @Override
    public AdminLoginVO login(AdminLoginDTO adminLoginDTO) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, adminLoginDTO.getUsername()));
        // 骨架阶段为演示明文比对，Phase 1 改为摘要加密（如 BCrypt/MD5+盐）
        if (admin == null || !admin.getPassword().equals(adminLoginDTO.getPassword())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        return AdminLoginVO.builder()
                .id(admin.getId())
                .token(token)
                .username(admin.getUsername())
                .realName(admin.getRealName())
                .build();
    }
}