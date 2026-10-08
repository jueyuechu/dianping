package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.constant.JwtClaimsConstant;
import com.dianping.common.constant.MessageConstant;
import com.dianping.common.exception.BaseException;
import com.dianping.common.properties.JwtProperties;
import com.dianping.common.utils.JwtUtil;
import com.dianping.dto.MerchantLoginDTO;
import com.dianping.entity.Merchant;
import com.dianping.entity.MerchantAccount;
import com.dianping.mapper.MerchantAccountMapper;
import com.dianping.mapper.MerchantMapper;
import com.dianping.service.MerchantService;
import com.dianping.vo.MerchantLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantAccountMapper merchantAccountMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public MerchantLoginVO login(MerchantLoginDTO merchantLoginDTO) {
        MerchantAccount account = merchantAccountMapper.selectOne(new LambdaQueryWrapper<MerchantAccount>()
                .eq(MerchantAccount::getUsername, merchantLoginDTO.getUsername()));
        // 骨架阶段为演示明文比对，Phase 1 改为摘要加密
        if (account == null || !account.getPassword().equals(merchantLoginDTO.getPassword())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }

        Merchant merchant = merchantMapper.selectById(account.getMerchantId());
        if (merchant == null || merchant.getStatus() == 0) {
            throw new BaseException("商户尚未通过入驻审核");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.MERCHANT_ID, account.getMerchantId());
        String token = JwtUtil.createJWT(jwtProperties.getMerchantSecretKey(), jwtProperties.getMerchantTtl(), claims);

        return MerchantLoginVO.builder()
                .id(account.getId())
                .token(token)
                .username(account.getUsername())
                .merchantId(account.getMerchantId())
                .role(account.getRole())
                .shopName(merchant.getName())
                .build();
    }
}