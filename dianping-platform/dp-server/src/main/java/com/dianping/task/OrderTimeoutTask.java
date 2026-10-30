package com.dianping.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dianping.entity.Groupbuy;
import com.dianping.entity.GroupbuyOrder;
import com.dianping.mapper.GroupbuyMapper;
import com.dianping.mapper.GroupbuyOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时关单：待支付超过 N 分钟自动关闭（状态 5 已过期）并回补库存
 */
@Slf4j
@Component
public class OrderTimeoutTask {

    @Autowired
    private GroupbuyOrderMapper groupbuyOrderMapper;

    @Autowired
    private GroupbuyMapper groupbuyMapper;

    @Value("${dianping.order.pay-timeout-minutes:15}")
    private long payTimeoutMinutes;

    @Scheduled(fixedDelayString = "${dianping.order.timeout-check-ms:60000}")
    @Transactional(rollbackFor = Exception.class)
    public void closeExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(payTimeoutMinutes);
        List<GroupbuyOrder> expired = groupbuyOrderMapper.selectList(new LambdaQueryWrapper<GroupbuyOrder>()
                .eq(GroupbuyOrder::getStatus, 1)
                .lt(GroupbuyOrder::getCreateTime, deadline)
                .last("LIMIT 100"));
        if (expired.isEmpty()) {
            return;
        }
        for (GroupbuyOrder order : expired) {
            order.setStatus(5);
            groupbuyOrderMapper.updateById(order);
            int qty = order.getQuantity() == null ? 1 : order.getQuantity();
            groupbuyMapper.update(null, new LambdaUpdateWrapper<Groupbuy>()
                    .eq(Groupbuy::getId, order.getGroupbuyId())
                    .ge(Groupbuy::getSold, qty)
                    .setSql("stock = stock + " + qty)
                    .setSql("sold = sold - " + qty));
        }
        log.info("超时关单 {} 笔", expired.size());
    }
}
