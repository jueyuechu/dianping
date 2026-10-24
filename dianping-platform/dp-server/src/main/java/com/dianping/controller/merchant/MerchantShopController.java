package com.dianping.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.common.result.Result;
import com.dianping.dto.ShopDTO;
import com.dianping.entity.Shop;
import com.dianping.mapper.ShopMapper;
import com.dianping.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户端店铺管理（多门店）
 */
@RestController
@RequestMapping("/api/merchant/shop")
public class MerchantShopController {

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private AuditService auditService;

    /**
     * 我的店铺列表
     */
    @GetMapping
    public Result<List<Shop>> list() {
        return Result.success(shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, currentMerchantId())
                .orderByDesc(Shop::getCreateTime)));
    }

    @GetMapping("/{id}")
    public Result<Shop> detail(@PathVariable Long id) {
        return Result.success(requireOwnedShop(id));
    }

    /**
     * 新增门店：默认不可见（休息），提交店铺审核
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Shop> create(@RequestBody @Valid ShopDTO dto) {
        Long merchantId = currentMerchantId();
        Shop shop = new Shop();
        applyFields(shop, dto);
        shop.setMerchantId(merchantId);
        shop.setStatus(0);
        shop.setSold(0);
        shop.setComments(0);
        shop.setScore(0);
        shop.setTasteScore(0);
        shop.setEnvScore(0);
        shop.setServiceScore(0);
        shop.setValueScore(0);
        shopMapper.insert(shop);
        auditService.submit(2, shop.getId(), merchantId);
        return Result.success(shop);
    }

    /**
     * 编辑门店（位置/照片等关键信息）：提交店铺审核
     */
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Shop> update(@PathVariable Long id, @RequestBody @Valid ShopDTO dto) {
        Shop shop = requireOwnedShop(id);
        applyFields(shop, dto);
        shopMapper.updateById(shop);
        auditService.submit(2, shop.getId(), currentMerchantId());
        return Result.success(shop);
    }

    /**
     * 营业时间 / 营业状态（非关键字段，直接生效，无需审核）
     */
    @PutMapping("/{id}/hours")
    public Result<Shop> updateHours(@PathVariable Long id,
                                    @RequestParam(required = false) String openHours,
                                    @RequestParam(required = false) Integer status) {
        Shop shop = requireOwnedShop(id);
        if (openHours != null) {
            shop.setOpenHours(openHours);
        }
        if (status != null) {
            shop.setStatus(status);
        }
        shopMapper.updateById(shop);
        return Result.success(shop);
    }

    private void applyFields(Shop shop, ShopDTO dto) {
        shop.setName(dto.getName());
        shop.setTypeId(dto.getTypeId());
        shop.setImages(dto.getImages());
        shop.setArea(dto.getArea());
        shop.setAddress(dto.getAddress());
        shop.setX(dto.getX());
        shop.setY(dto.getY());
        shop.setAvgPrice(dto.getAvgPrice());
        shop.setOpenHours(dto.getOpenHours());
        shop.setPhone(dto.getPhone());
    }

    private Long currentMerchantId() {
        Long id = BaseContext.getCurrentId();
        if (id == null) {
            throw new BaseException(401, "未登录或登录已过期");
        }
        return id;
    }

    private Shop requireOwnedShop(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || !shop.getMerchantId().equals(currentMerchantId())) {
            throw new BaseException(404, "店铺不存在");
        }
        return shop;
    }
}