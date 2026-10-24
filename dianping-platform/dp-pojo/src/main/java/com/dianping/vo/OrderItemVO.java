package com.dianping.vo;

import com.dianping.entity.GroupbuyOrder;
import lombok.Data;

/**
 * 用户订单列表项 VO（订单 + 店铺名 + 团购名）
 */
@Data
public class OrderItemVO {

    private Long id;

    private String orderNo;

    private Long shopId;

    private String shopName;

    private Long groupbuyId;

    private String groupbuyName;

    private Integer quantity;

    private java.math.BigDecimal amount;

    private Integer status;

    private String verifyCode;

    private java.time.LocalDateTime createTime;

    public static OrderItemVO of(GroupbuyOrder order) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setShopId(order.getShopId());
        vo.setGroupbuyId(order.getGroupbuyId());
        vo.setQuantity(order.getQuantity());
        vo.setAmount(order.getAmount());
        vo.setStatus(order.getStatus());
        vo.setVerifyCode(order.getVerifyCode());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }
}