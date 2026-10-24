package com.dianping.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.common.result.Result;
import com.dianping.dto.GroupbuyDTO;
import com.dianping.entity.Groupbuy;
import com.dianping.entity.GroupbuyRule;
import com.dianping.entity.Shop;
import com.dianping.mapper.GroupbuyMapper;
import com.dianping.mapper.GroupbuyRuleMapper;
import com.dianping.mapper.ShopMapper;
import com.dianping.service.AuditService;
import com.dianping.vo.GroupbuyDetailVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户端团购管理（团购一律先审后发）
 */
@RestController
@RequestMapping("/api/merchant/groupbuy")
public class MerchantGroupbuyController {

    @Autowired
    private GroupbuyMapper groupbuyMapper;

    @Autowired
    private GroupbuyRuleMapper groupbuyRuleMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private AuditService auditService;

    @GetMapping("/list")
    public Result<List<Groupbuy>> list(@RequestParam Long shopId) {
        requireOwnedShop(shopId);
        return Result.success(groupbuyMapper.selectList(new LambdaQueryWrapper<Groupbuy>()
                .eq(Groupbuy::getShopId, shopId)
                .orderByDesc(Groupbuy::getCreateTime)));
    }

    @GetMapping("/{id}")
    public Result<GroupbuyDetailVO> detail(@PathVariable Long id) {
        Groupbuy groupbuy = requireOwnedGroupbuy(id);
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
     * 创建团购：状态置为审核中（2），提交团购审核
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Groupbuy> create(@RequestBody @Valid GroupbuyDTO dto) {
        requireOwnedShop(dto.getShopId());
        if (dto.getOriginalPrice() != null && dto.getGroupbuyPrice().compareTo(dto.getOriginalPrice()) > 0) {
            throw new BaseException("团购价不能高于原价");
        }
        Groupbuy groupbuy = new Groupbuy();
        applyFields(groupbuy, dto);
        groupbuy.setStatus(2);
        groupbuy.setSold(0);
        groupbuyMapper.insert(groupbuy);
        saveRule(groupbuy.getId(), dto);
        auditService.submit(4, groupbuy.getId(), BaseContext.getCurrentId());
        return Result.success(groupbuy);
    }

    /**
     * 编辑团购（价格/规则）：重新进入审核
     */
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Groupbuy> update(@PathVariable Long id, @RequestBody @Valid GroupbuyDTO dto) {
        Groupbuy groupbuy = requireOwnedGroupbuy(id);
        applyFields(groupbuy, dto);
        groupbuy.setStatus(2);
        groupbuyMapper.updateById(groupbuy);
        groupbuyRuleMapper.delete(new LambdaQueryWrapper<GroupbuyRule>()
                .eq(GroupbuyRule::getGroupbuyId, id));
        saveRule(id, dto);
        auditService.submit(4, id, BaseContext.getCurrentId());
        return Result.success(groupbuy);
    }

    /**
     * 上下架（已审核通过的团购可由商户自行下架；重新上架无需重审）
     */
    @PutMapping("/{id}/status")
    public Result<Groupbuy> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Groupbuy groupbuy = requireOwnedGroupbuy(id);
        if (groupbuy.getStatus() == 2) {
            throw new BaseException("团购审核中，暂不能操作上下架");
        }
        groupbuy.setStatus(status);
        groupbuyMapper.updateById(groupbuy);
        return Result.success(groupbuy);
    }

    /**
     * 库存设置（无需审核）
     */
    @PutMapping("/{id}/stock")
    public Result<Groupbuy> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        Groupbuy groupbuy = requireOwnedGroupbuy(id);
        groupbuy.setStock(stock);
        groupbuyMapper.updateById(groupbuy);
        return Result.success(groupbuy);
    }

    /**
     * 使用 / 退款规则（直接生效）
     */
    @PutMapping("/{id}/rule")
    public Result<GroupbuyRule> updateRule(@PathVariable Long id, @RequestBody GroupbuyDTO dto) {
        requireOwnedGroupbuy(id);
        GroupbuyRule rule = groupbuyRuleMapper.selectOne(new LambdaQueryWrapper<GroupbuyRule>()
                .eq(GroupbuyRule::getGroupbuyId, id));
        if (rule == null) {
            rule = new GroupbuyRule();
            rule.setGroupbuyId(id);
            fillRule(rule, dto);
            groupbuyRuleMapper.insert(rule);
        } else {
            fillRule(rule, dto);
            groupbuyRuleMapper.updateById(rule);
        }
        return Result.success(rule);
    }

    private void applyFields(Groupbuy groupbuy, GroupbuyDTO dto) {
        groupbuy.setShopId(dto.getShopId());
        groupbuy.setName(dto.getName());
        groupbuy.setImage(dto.getImage());
        groupbuy.setOriginalPrice(dto.getOriginalPrice());
        groupbuy.setGroupbuyPrice(dto.getGroupbuyPrice());
        groupbuy.setContent(dto.getContent());
        groupbuy.setType(dto.getType() == null ? 1 : dto.getType());
        groupbuy.setStock(dto.getStock() == null ? 0 : dto.getStock());
        groupbuy.setVerifyType(dto.getVerifyType() == null ? 3 : dto.getVerifyType());
    }

    private void saveRule(Long groupbuyId, GroupbuyDTO dto) {
        GroupbuyRule rule = new GroupbuyRule();
        rule.setGroupbuyId(groupbuyId);
        fillRule(rule, dto);
        groupbuyRuleMapper.insert(rule);
    }

    private void fillRule(GroupbuyRule rule, GroupbuyDTO dto) {
        rule.setHolidayUsable(dto.getHolidayUsable() == null ? 1 : dto.getHolidayUsable());
        rule.setPerUserLimit(dto.getPerUserLimit() == null ? 0 : dto.getPerUserLimit());
        rule.setPerTableLimit(dto.getPerTableLimit() == null ? 1 : dto.getPerTableLimit());
        rule.setValidStart(dto.getValidStart());
        rule.setValidEnd(dto.getValidEnd());
        rule.setRefundRule(dto.getRefundRule() == null ? 1 : dto.getRefundRule());
    }

    private void requireOwnedShop(Long shopId) {
        Long merchantId = BaseContext.getCurrentId();
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || !shop.getMerchantId().equals(merchantId)) {
            throw new BaseException(404, "店铺不存在");
        }
    }

    private Groupbuy requireOwnedGroupbuy(Long id) {
        Groupbuy groupbuy = groupbuyMapper.selectById(id);
        if (groupbuy == null) {
            throw new BaseException(404, "团购不存在");
        }
        requireOwnedShop(groupbuy.getShopId());
        return groupbuy;
    }
}