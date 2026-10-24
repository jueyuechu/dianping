package com.dianping.service;

import com.dianping.dto.MerchantLoginDTO;
import com.dianping.dto.MerchantRegisterDTO;
import com.dianping.vo.MerchantLoginVO;

import java.util.Map;

public interface MerchantService {

    MerchantLoginVO login(MerchantLoginDTO merchantLoginDTO);

    /**
     * 商户入驻申请：创建商户（待审核）+ 登录账号 + 审核任务
     */
    Long register(MerchantRegisterDTO dto);

    /**
     * 入驻审核进度
     */
    Map<String, Object> applyStatus();
}