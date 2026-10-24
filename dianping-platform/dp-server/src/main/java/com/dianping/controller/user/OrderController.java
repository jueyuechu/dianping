package com.dianping.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.common.result.Result;
import com.dianping.dto.PayDTO;
import com.dianping.entity.GroupbuyOrder;
import com.dianping.mapper.GroupbuyOrderMapper;
import com.dianping.service.OrderService;
import com.dianping.vo.OrderItemVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端订单接口（需登录）
 */
@RestController
@RequestMapping("/api/user/order")
public class OrderController {

    @Autowired
    private GroupbuyOrderMapper groupbuyOrderMapper;

    @Autowired
    private OrderService orderService;

    /**
     * 订单列表（可按状态筛选：1 待支付 2 待使用 3 已使用 4 已退款 5 已过期）
     */
    @GetMapping("/list")
    public Result<List<OrderItemVO>> list(@RequestParam(required = false) Integer status) {
        return Result.success(orderService.list(status));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public Result<GroupbuyOrder> detail(@PathVariable Long id) {
        GroupbuyOrder order = groupbuyOrderMapper.selectOne(new LambdaQueryWrapper<GroupbuyOrder>()
                .eq(GroupbuyOrder::getId, id)
                .eq(GroupbuyOrder::getUserId, BaseContext.getCurrentId()));
        if (order == null) {
            throw new BaseException(404, "订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 支付（骨架阶段模拟支付；Phase 1 返回微信支付参数并由回调更新订单）
     */
    @PostMapping("/pay")
    public Result<Map<String, Object>> pay(@RequestBody @Valid PayDTO dto) {
        return Result.success(orderService.pay(dto.getOrderId()));
    }

    /**
     * 获取核销码 / 二维码内容
     */
    @GetMapping("/{id}/verify-code")
    public Result<Map<String, Object>> verifyCode(@PathVariable Long id) {
        Map<String, Object> data = new HashMap<>();
        String code = orderService.verifyCode(id);
        data.put("verifyCode", code);
        data.put("qrCode", "https://dianping.local/verify?code=" + code);
        return Result.success(data);
    }
}