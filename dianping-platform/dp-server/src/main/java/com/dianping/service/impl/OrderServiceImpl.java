package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.dto.GroupbuyOrderDTO;
import com.dianping.entity.Groupbuy;
import com.dianping.entity.GroupbuyOrder;
import com.dianping.entity.Payment;
import com.dianping.mapper.GroupbuyMapper;
import com.dianping.mapper.GroupbuyOrderMapper;
import com.dianping.mapper.PaymentMapper;
import com.dianping.mapper.ShopMapper;
import com.dianping.entity.Shop;
import com.dianping.service.OrderService;
import com.dianping.vo.OrderItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private GroupbuyMapper groupbuyMapper;

    @Autowired
    private GroupbuyOrderMapper groupbuyOrderMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private com.dianping.mq.NotificationProducer notificationProducer;

    private static final Random RANDOM = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupbuyOrder create(GroupbuyOrderDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(401, "未登录或登录已过期");
        }
        Groupbuy groupbuy = groupbuyMapper.selectById(dto.getGroupbuyId());
        if (groupbuy == null) {
            throw new BaseException(404, "团购不存在");
        }
        if (groupbuy.getStatus() == null || groupbuy.getStatus() != 1) {
            throw new BaseException("团购未上架");
        }
        // 乐观扣减库存：stock >= 购买数量 才会更新成功，防止超卖
        int updated = groupbuyMapper.update(null, new LambdaUpdateWrapper<Groupbuy>()
                .eq(Groupbuy::getId, groupbuy.getId())
                .eq(Groupbuy::getStatus, 1)
                .ge(Groupbuy::getStock, dto.getQuantity())
                .setSql("stock = stock - " + dto.getQuantity())
                .setSql("sold = sold + " + dto.getQuantity()));
        if (updated == 0) {
            throw new BaseException("库存不足");
        }

        GroupbuyOrder order = new GroupbuyOrder();
        order.setOrderNo("DP" + System.currentTimeMillis() + (100 + RANDOM.nextInt(900)));
        order.setUserId(userId);
        order.setGroupbuyId(groupbuy.getId());
        order.setShopId(groupbuy.getShopId());
        order.setQuantity(dto.getQuantity());
        order.setAmount(groupbuy.getGroupbuyPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
        order.setStatus(1);
        order.setVerifyCode(String.format("%06d", RANDOM.nextInt(1000000)));
        groupbuyOrderMapper.insert(order);
        notificationProducer.orderCreated(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> pay(Long orderId) {
        Long userId = BaseContext.getCurrentId();
        GroupbuyOrder order = groupbuyOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BaseException(404, "订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new BaseException("当前订单状态不可支付");
        }

        // 骨架阶段：模拟支付成功，直接流转为「待使用」并落支付流水
        // Phase 1 接入微信支付 V3：此处只返回 prepayId/paySign，由支付回调 notify 更新订单
        order.setStatus(2);
        groupbuyOrderMapper.updateById(order);

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setPayNo("MOCK" + System.currentTimeMillis() + (100 + RANDOM.nextInt(900)));
        payment.setAmount(order.getAmount());
        payment.setChannel(1);
        payment.setStatus(2);
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("prepayId", "mock_prepay_" + order.getOrderNo());
        result.put("nonceStr", String.valueOf(System.currentTimeMillis()));
        result.put("paySign", "MOCK_SIGN");
        return result;
    }

    @Override
    public String verifyCode(Long orderId) {
        Long userId = BaseContext.getCurrentId();
        GroupbuyOrder order = groupbuyOrderMapper.selectOne(new LambdaQueryWrapper<GroupbuyOrder>()
                .eq(GroupbuyOrder::getId, orderId)
                .eq(GroupbuyOrder::getUserId, userId));
        if (order == null) {
            throw new BaseException(404, "订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() < 2) {
            throw new BaseException("订单尚未支付");
        }
        if (order.getStatus() == 4 || order.getStatus() == 5) {
            throw new BaseException("订单已退款或已过期，核销码不可用");
        }
        return order.getVerifyCode();
    }

    @Override
    public List<OrderItemVO> list(Integer status) {
        Long userId = BaseContext.getCurrentId();
        List<GroupbuyOrder> orders = groupbuyOrderMapper.selectList(new LambdaQueryWrapper<GroupbuyOrder>()
                .eq(GroupbuyOrder::getUserId, userId)
                .eq(status != null, GroupbuyOrder::getStatus, status)
                .orderByDesc(GroupbuyOrder::getCreateTime));
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> shopIds = orders.stream().map(GroupbuyOrder::getShopId).collect(Collectors.toSet());
        Set<Long> groupbuyIds = orders.stream().map(GroupbuyOrder::getGroupbuyId).collect(Collectors.toSet());

        Map<Long, String> shopNames = shopMapper.selectBatchIds(shopIds).stream()
                .collect(Collectors.toMap(Shop::getId, Shop::getName, (a, b) -> a));
        Map<Long, String> groupbuyNames = groupbuyMapper.selectBatchIds(groupbuyIds).stream()
                .collect(Collectors.toMap(Groupbuy::getId, Groupbuy::getName, (a, b) -> a));

        return orders.stream().map(o -> {
            OrderItemVO vo = OrderItemVO.of(o);
            vo.setShopName(shopNames.get(o.getShopId()));
            vo.setGroupbuyName(groupbuyNames.get(o.getGroupbuyId()));
            return vo;
        }).collect(Collectors.toList());
    }
}