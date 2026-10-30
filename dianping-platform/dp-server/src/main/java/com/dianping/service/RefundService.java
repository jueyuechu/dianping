package com.dianping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.vo.RefundVO;

/**
 * 退款：用户申请 + 管理端审核
 */
public interface RefundService {

    Long apply(Long userId, Long orderId, String reason);

    Page<RefundVO> myPage(long current, long size, Long userId);

    Page<RefundVO> adminPage(long current, long size, Integer status);

    void approve(Long refundId);

    void reject(Long refundId, String reason);
}
