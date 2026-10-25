package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.constant.JwtClaimsConstant;
import com.dianping.common.constant.MessageConstant;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.common.properties.JwtProperties;
import com.dianping.common.utils.JwtUtil;
import com.dianping.common.utils.PasswordUtil;
import com.dianping.dto.MerchantLoginDTO;
import com.dianping.dto.MerchantRegisterDTO;
import com.dianping.entity.AuditTask;
import com.dianping.entity.Merchant;
import com.dianping.entity.MerchantAccount;
import com.dianping.mapper.AuditTaskMapper;
import com.dianping.mapper.MerchantAccountMapper;
import com.dianping.mapper.MerchantMapper;
import com.dianping.service.AuditService;
import com.dianping.service.MerchantService;
import com.dianping.service.TokenService;
import com.dianping.vo.MerchantLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantAccountMapper merchantAccountMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private AuditTaskMapper auditTaskMapper;

    @Autowired
    private AuditService auditService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private TokenService tokenService;

    @Override
    public MerchantLoginVO login(MerchantLoginDTO merchantLoginDTO) {
        MerchantAccount account = merchantAccountMapper.selectOne(new LambdaQueryWrapper<MerchantAccount>()
                .eq(MerchantAccount::getUsername, merchantLoginDTO.getUsername()));
        if (account == null || !PasswordUtil.matches(merchantLoginDTO.getPassword(), account.getPassword())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }
        // 历史明文密码在首次登录成功后自动升级为 BCrypt
        if (!PasswordUtil.isEncoded(account.getPassword())) {
            account.setPassword(PasswordUtil.encode(merchantLoginDTO.getPassword()));
            merchantAccountMapper.updateById(account);
        }

        Merchant merchant = merchantMapper.selectById(account.getMerchantId());
        if (merchant == null) {
            throw new BaseException(404, "商户不存在");
        }
        if (merchant.getStatus() == 2) {
            throw new BaseException("商户已被冻结，请联系平台客服");
        }
        // 待审核（0）允许登录，仅可查看入驻进度；经营类接口由前端与业务侧限制

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.MERCHANT_ID, account.getMerchantId());
        String token = JwtUtil.createJWT(jwtProperties.getMerchantSecretKey(), jwtProperties.getMerchantTtl(), claims);
        tokenService.save(TokenService.SCOPE_MERCHANT, token, jwtProperties.getMerchantTtl());

        return MerchantLoginVO.builder()
                .id(account.getId())
                .token(token)
                .username(account.getUsername())
                .merchantId(account.getMerchantId())
                .role(account.getRole())
                .shopName(merchant.getName())
                .merchantStatus(merchant.getStatus())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(MerchantRegisterDTO dto) {
        Long exists = merchantAccountMapper.selectCount(new LambdaQueryWrapper<MerchantAccount>()
                .eq(MerchantAccount::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BaseException("登录账号已被占用");
        }

        Merchant merchant = new Merchant();
        merchant.setName(dto.getName());
        merchant.setLicenseNo(dto.getLicenseNo());
        merchant.setLicenseImg(dto.getLicenseImg());
        merchant.setLegalPerson(dto.getLegalPerson());
        merchant.setContactPhone(dto.getContactPhone());
        // 入驻资料提交后待审核
        merchant.setStatus(0);
        merchantMapper.insert(merchant);

        MerchantAccount account = new MerchantAccount();
        account.setMerchantId(merchant.getId());
        account.setUsername(dto.getUsername());
        account.setPassword(PasswordUtil.encode(dto.getPassword()));
        account.setRole(1);
        account.setStatus(1);
        merchantAccountMapper.insert(account);

        // 进入统一审核队列（biz_type=1 商户入驻）
        auditService.submit(1, merchant.getId(), null);
        return merchant.getId();
    }

    @Override
    public Map<String, Object> applyStatus() {
        Long merchantId = BaseContext.getCurrentId();
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BaseException(404, "商户不存在");
        }
        AuditTask task = auditTaskMapper.selectOne(new LambdaQueryWrapper<AuditTask>()
                .eq(AuditTask::getBizType, 1)
                .eq(AuditTask::getBizId, merchantId)
                .orderByDesc(AuditTask::getCreateTime)
                .last("LIMIT 1"));

        Map<String, Object> data = new HashMap<>();
        data.put("merchantId", merchantId);
        data.put("merchantName", merchant.getName());
        data.put("merchantStatus", merchant.getStatus());
        data.put("auditStatus", task == null ? null : task.getStatus());
        data.put("reason", task == null ? null : task.getReason());
        data.put("submitTime", task == null ? null : task.getCreateTime());
        return data;
    }
}