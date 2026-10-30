package com.dianping.controller.user;

import com.dianping.common.result.Result;
import com.dianping.dto.PayNotifyDTO;
import com.dianping.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 支付回调（微信支付 V3 接入前的骨架）
 * 生产环境需校验微信签名/证书后再处理
 */
@RestController
@RequestMapping("/api/user/pay")
public class PayNotifyController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/notify")
    public Result<Void> notify(@RequestBody PayNotifyDTO dto) {
        orderService.payNotify(dto);
        return Result.success();
    }
}
