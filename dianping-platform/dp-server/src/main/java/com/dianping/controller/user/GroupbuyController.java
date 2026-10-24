package com.dianping.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BaseException;
import com.dianping.common.result.Result;
import com.dianping.dto.GroupbuyOrderDTO;
import com.dianping.entity.Groupbuy;
import com.dianping.entity.GroupbuyOrder;
import com.dianping.entity.GroupbuyRule;
import com.dianping.entity.Shop;
import com.dianping.mapper.GroupbuyMapper;
import com.dianping.mapper.GroupbuyRuleMapper;
import com.dianping.mapper.ShopMapper;
import com.dianping.service.OrderService;
import com.dianping.vo.GroupbuyDetailVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端团购接口（详情需登录，下单需登录）
 */
@RestController
@RequestMapping("/api/user/groupbuy")
public class GroupbuyController {

    @Autowired
    private GroupbuyMapper groupbuyMapper;

    @Autowired
    private GroupbuyRuleMapper groupbuyRuleMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private OrderService orderService;

    /**
     * 团购详情（含使用/退款规则与店铺信息）
     */
    @GetMapping("/{id}")
    public Result<GroupbuyDetailVO> detail(@PathVariable Long id) {
        Groupbuy groupbuy = groupbuyMapper.selectById(id);
        if (groupbuy == null) {
            throw new BaseException(404, "团购不存在");
        }
        GroupbuyRule rule = groupbuyRuleMapper.selectOne(new LambdaQueryWrapper<GroupbuyRule>()
                .eq(GroupbuyRule::getGroupbuyId, id));
        Shop shop = shopMapper.selectById(groupbuy.getShopId());
        return Result.success(GroupbuyDetailVO.builder()
                .groupbuy(groupbuy)
                .rule(rule)
                .shopId(groupbuy.getShopId())
                .shopName(shop == null ? null : shop.getName())
                .build());
    }

    /**
     * 下单（创建订单，返回订单号与订单 id）
     */
    @PostMapping("/order")
    public Result<GroupbuyOrder> order(@RequestBody @Valid GroupbuyOrderDTO dto) {
        return Result.success(orderService.create(dto));
    }
}