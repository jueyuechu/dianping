package com.dianping.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.common.result.Result;
import com.dianping.dto.VerifyDTO;
import com.dianping.entity.Groupbuy;
import com.dianping.entity.GroupbuyOrder;
import com.dianping.entity.Shop;
import com.dianping.entity.VerificationRecord;
import com.dianping.mapper.GroupbuyMapper;
import com.dianping.mapper.GroupbuyOrderMapper;
import com.dianping.mapper.ShopMapper;
import com.dianping.mapper.VerificationRecordMapper;
import com.dianping.vo.OrderItemVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商户端订单与核销接口
 */
@RestController
@RequestMapping("/api/merchant/order")
public class MerchantOrderController {

    @Autowired
    private GroupbuyOrderMapper groupbuyOrderMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private VerificationRecordMapper verificationRecordMapper;

    @Autowired
    private GroupbuyMapper groupbuyMapper;

    /**
     * 团购订单列表（按当前商户名下店铺过滤，含团购名/店铺名）
     */
    @GetMapping("/list")
    public Result<Page<OrderItemVO>> list(@RequestParam(defaultValue = "1") long current,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) Integer status) {
        List<Long> shopIds = currentShopIds();
        Page<GroupbuyOrder> page = new Page<>(current, size);
        if (shopIds.isEmpty()) {
            return Result.success(new Page<OrderItemVO>(current, size));
        }
        Page<GroupbuyOrder> orderPage = groupbuyOrderMapper.selectPage(page, new LambdaQueryWrapper<GroupbuyOrder>()
                .in(GroupbuyOrder::getShopId, shopIds)
                .eq(status != null, GroupbuyOrder::getStatus, status)
                .orderByDesc(GroupbuyOrder::getCreateTime));

        List<OrderItemVO> items = orderPage.getRecords().stream().map(OrderItemVO::of).collect(Collectors.toList());
        if (!items.isEmpty()) {
            Set<Long> groupbuyIds = items.stream().map(OrderItemVO::getGroupbuyId).collect(Collectors.toSet());
            Map<Long, String> names = groupbuyMapper.selectBatchIds(groupbuyIds).stream()
                    .collect(Collectors.toMap(Groupbuy::getId, Groupbuy::getName, (a, b) -> a));
            Map<Long, String> shopNames = shopMapper.selectBatchIds(shopIds).stream()
                    .collect(Collectors.toMap(Shop::getId, Shop::getName, (a, b) -> a));
            items.forEach(i -> {
                i.setGroupbuyName(names.get(i.getGroupbuyId()));
                i.setShopName(shopNames.get(i.getShopId()));
            });
        }
        Page<OrderItemVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(items);
        return Result.success(voPage);
    }

    /**
     * 核销：输入/扫描核销码，订单 待使用 → 已使用
     */
    @PostMapping("/verify")
    @Transactional(rollbackFor = Exception.class)
    public Result<GroupbuyOrder> verify(@RequestBody @Valid VerifyDTO dto) {
        GroupbuyOrder order = groupbuyOrderMapper.selectOne(new LambdaQueryWrapper<GroupbuyOrder>()
                .eq(GroupbuyOrder::getVerifyCode, dto.getVerifyCode()));
        if (order == null) {
            throw new BaseException("核销码无效");
        }
        if (!currentShopIds().contains(order.getShopId())) {
            throw new BaseException(403, "无权核销该店铺订单");
        }
        if (order.getStatus() == null || order.getStatus() != 2) {
            throw new BaseException("订单状态不可核销（已使用/已退款/已过期）");
        }
        order.setStatus(3);
        groupbuyOrderMapper.updateById(order);

        VerificationRecord record = new VerificationRecord();
        record.setOrderId(order.getId());
        record.setOperatorId(BaseContext.getCurrentId());
        record.setVerifyTime(LocalDateTime.now());
        record.setMethod(dto.getMethod() == null ? 2 : dto.getMethod());
        verificationRecordMapper.insert(record);
        return Result.success(order);
    }

    /**
     * 核销记录
     */
    @GetMapping("/verify-records")
    public Result<Page<VerificationRecord>> verifyRecords(@RequestParam(defaultValue = "1") long current,
                                                          @RequestParam(defaultValue = "10") long size) {
        List<Long> shopIds = currentShopIds();
        Page<VerificationRecord> page = new Page<>(current, size);
        if (shopIds.isEmpty()) {
            return Result.success(page);
        }
        List<Long> orderIds = groupbuyOrderMapper.selectList(new LambdaQueryWrapper<GroupbuyOrder>()
                        .in(GroupbuyOrder::getShopId, shopIds))
                .stream().map(GroupbuyOrder::getId).collect(Collectors.toList());
        if (orderIds.isEmpty()) {
            return Result.success(page);
        }
        return Result.success(verificationRecordMapper.selectPage(page,
                new LambdaQueryWrapper<VerificationRecord>()
                        .in(VerificationRecord::getOrderId, orderIds)
                        .orderByDesc(VerificationRecord::getVerifyTime)));
    }

    /**
     * 当前登录商户名下的店铺 id（BaseContext 存的是商户 id）
     */
    private List<Long> currentShopIds() {
        Long merchantId = BaseContext.getCurrentId();
        if (merchantId == null) {
            throw new BaseException(401, "未登录或登录已过期");
        }
        return shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getMerchantId, merchantId))
                .stream().map(Shop::getId).collect(Collectors.toList());
    }
}