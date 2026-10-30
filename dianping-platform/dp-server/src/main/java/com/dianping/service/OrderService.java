package com.dianping.service;

import com.dianping.dto.GroupbuyOrderDTO;
import com.dianping.dto.PayNotifyDTO;
import com.dianping.entity.GroupbuyOrder;
import com.dianping.vo.OrderItemVO;

import java.util.List;
import java.util.Map;

public interface OrderService {

    /**
     * 创建团购订单（扣减库存 + 生成核销码）
     */
    GroupbuyOrder create(GroupbuyOrderDTO dto);

    /**
     * 支付订单（骨架阶段为模拟支付，Phase 1 接入微信支付 V3）
     */
    Map<String, Object> pay(Long orderId);

    /**
     * 支付回调（幂等）：支付成功后订单流转为待使用并落支付流水
     */
    void payNotify(PayNotifyDTO dto);

    /**
     * 查询核销码
     */
    String verifyCode(Long orderId);

    /**
     * 我的订单（含店铺名/团购名）
     */
    List<OrderItemVO> list(Integer status);
}