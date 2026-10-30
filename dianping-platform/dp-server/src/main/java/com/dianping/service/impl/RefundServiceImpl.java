package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.exception.BaseException;
import com.dianping.entity.GroupbuyOrder;
import com.dianping.entity.Refund;
import com.dianping.mapper.GroupbuyOrderMapper;
import com.dianping.mapper.RefundMapper;
import com.dianping.service.RefundService;
import com.dianping.vo.RefundVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RefundServiceImpl implements RefundService {

    private static final int REFUND_APPLY = 1;
    private static final int REFUND_DONE = 4;
    private static final int REFUND_REJECT = 5;

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private GroupbuyOrderMapper groupbuyOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(Long userId, Long orderId, String reason) {
        GroupbuyOrder order = groupbuyOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BaseException(404, "订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 2) {
            throw new BaseException("仅待使用的订单可以申请退款");
        }
        long active = refundMapper.selectCount(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getOrderId, orderId)
                .in(Refund::getStatus, 1, 2, 3));
        if (active > 0) {
            throw new BaseException("该订单已提交退款申请，请勿重复提交");
        }

        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setRefundNo("RF" + System.currentTimeMillis());
        refund.setAmount(order.getAmount());
        refund.setReason(reason == null || reason.isBlank() ? "用户申请退款" : reason);
        refund.setStatus(REFUND_APPLY);
        refund.setApplyTime(LocalDateTime.now());
        refundMapper.insert(refund);
        return refund.getId();
    }

    @Override
    public Page<RefundVO> myPage(long current, long size, Long userId) {
        List<Long> orderIds = groupbuyOrderMapper.selectList(new LambdaQueryWrapper<GroupbuyOrder>()
                        .eq(GroupbuyOrder::getUserId, userId))
                .stream().map(GroupbuyOrder::getId).collect(Collectors.toList());
        if (orderIds.isEmpty()) {
            return new Page<>(current, size);
        }
        return toVoPage(current, size, new LambdaQueryWrapper<Refund>()
                .in(Refund::getOrderId, orderIds)
                .orderByDesc(Refund::getCreateTime));
    }

    @Override
    public Page<RefundVO> adminPage(long current, long size, Integer status) {
        return toVoPage(current, size, new LambdaQueryWrapper<Refund>()
                .eq(status != null, Refund::getStatus, status)
                .orderByDesc(Refund::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long refundId) {
        Refund refund = requireProcessing(refundId);
        GroupbuyOrder order = groupbuyOrderMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BaseException(404, "订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 2) {
            throw new BaseException("订单已使用或状态变更，无法退款");
        }
        order.setStatus(4);
        groupbuyOrderMapper.updateById(order);

        refund.setStatus(REFUND_DONE);
        refund.setRefundTime(LocalDateTime.now());
        refundMapper.updateById(refund);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long refundId, String reason) {
        Refund refund = requireProcessing(refundId);
        refund.setStatus(REFUND_REJECT);
        refund.setReason(reason == null || reason.isBlank() ? "退款申请被驳回" : reason);
        refundMapper.updateById(refund);
    }

    private Refund requireProcessing(Long refundId) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BaseException(404, "退款申请不存在");
        }
        if (refund.getStatus() == null || refund.getStatus() > 2) {
            throw new BaseException("该退款申请已处理");
        }
        return refund;
    }

    private Page<RefundVO> toVoPage(long current, long size, LambdaQueryWrapper<Refund> wrapper) {
        Page<Refund> page = refundMapper.selectPage(new Page<>(current, size), wrapper);
        Set<Long> orderIds = page.getRecords().stream().map(Refund::getOrderId).collect(Collectors.toSet());
        Map<Long, String> orderNos = orderIds.isEmpty() ? Map.of()
                : groupbuyOrderMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(GroupbuyOrder::getId, GroupbuyOrder::getOrderNo, (a, b) -> a));
        Page<RefundVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(r -> RefundVO.of(r, orderNos.getOrDefault(r.getOrderId(), "")))
                .collect(Collectors.toList()));
        return voPage;
    }
}
