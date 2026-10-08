package com.dianping.service;

import com.dianping.dto.MerchantLoginDTO;
import com.dianping.vo.MerchantLoginVO;

public interface MerchantService {

    MerchantLoginVO login(MerchantLoginDTO merchantLoginDTO);
}