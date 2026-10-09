package com.dianping.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.result.Result;
import com.dianping.entity.Merchant;
import com.dianping.entity.Shop;
import com.dianping.mapper.MerchantMapper;
import com.dianping.mapper.ShopMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端商户管理：列表、冻结/解冻、店铺列表
 */
@RestController
@RequestMapping("/api/admin/merchant")
public class AdminMerchantController {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ShopMapper shopMapper;

    @GetMapping("/list")
    public Result<Page<Merchant>> list(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer status) {
        Page<Merchant> page = new Page<>(current, size);
        return Result.success(merchantMapper.selectPage(page, new LambdaQueryWrapper<Merchant>()
                .like(keyword != null && !keyword.isBlank(), Merchant::getName, keyword)
                .eq(status != null, Merchant::getStatus, status)
                .orderByDesc(Merchant::getCreateTime)));
    }

    @PostMapping("/{id}/freeze")
    public Result<Void> freeze(@PathVariable Long id) {
        return updateStatus(id, 2);
    }

    @PostMapping("/{id}/unfreeze")
    public Result<Void> unfreeze(@PathVariable Long id) {
        return updateStatus(id, 1);
    }

    private Result<Void> updateStatus(Long id, Integer status) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            return Result.error(404, "商户不存在");
        }
        merchant.setStatus(status);
        merchantMapper.updateById(merchant);
        return Result.success();
    }

    @GetMapping("/shop/list")
    public Result<Page<Shop>> shopList(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Long merchantId) {
        Page<Shop> page = new Page<>(current, size);
        return Result.success(shopMapper.selectPage(page, new LambdaQueryWrapper<Shop>()
                .eq(merchantId != null, Shop::getMerchantId, merchantId)
                .orderByDesc(Shop::getCreateTime)));
    }
}