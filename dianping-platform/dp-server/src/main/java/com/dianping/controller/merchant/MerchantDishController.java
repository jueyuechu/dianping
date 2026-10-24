package com.dianping.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.common.result.Result;
import com.dianping.dto.DishDTO;
import com.dianping.entity.Dish;
import com.dianping.entity.DishCategory;
import com.dianping.entity.Shop;
import com.dianping.mapper.DishCategoryMapper;
import com.dianping.mapper.DishMapper;
import com.dianping.mapper.ShopMapper;
import com.dianping.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户端菜品管理（分类 / 菜品 CRUD / 上下架）
 * 新增菜品需审核；上下架、库存调整由商户直接操作
 */
@RestController
@RequestMapping("/api/merchant/dish")
public class MerchantDishController {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishCategoryMapper dishCategoryMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private AuditService auditService;

    /**
     * 菜品列表（可按分类过滤）
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(@RequestParam Long shopId,
                                   @RequestParam(required = false) Long categoryId) {
        requireOwnedShop(shopId);
        return Result.success(dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getShopId, shopId)
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .orderByAsc(Dish::getCategoryId)
                .orderByDesc(Dish::getCreateTime)));
    }

    /**
     * 新增菜品：默认下架（不可见），提交菜品审核
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Dish> create(@RequestBody @Valid DishDTO dto) {
        requireOwnedShop(dto.getShopId());
        Dish dish = new Dish();
        applyFields(dish, dto);
        dish.setSold(0);
        dish.setStatus(0);
        dishMapper.insert(dish);
        auditService.submit(3, dish.getId(), BaseContext.getCurrentId());
        return Result.success(dish);
    }

    /**
     * 编辑菜品（名称/图片/价格/描述）：直接生效，Phase 1 可加修改审核
     */
    @PutMapping("/{id}")
    public Result<Dish> update(@PathVariable Long id, @RequestBody @Valid DishDTO dto) {
        Dish dish = requireOwnedDish(id);
        applyFields(dish, dto);
        dishMapper.updateById(dish);
        return Result.success(dish);
    }

    /**
     * 上下架 / 售罄（无需审核）
     */
    @PutMapping("/{id}/status")
    public Result<Dish> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Dish dish = requireOwnedDish(id);
        dish.setStatus(status);
        dishMapper.updateById(dish);
        return Result.success(dish);
    }

    /**
     * 删除菜品（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Dish dish = requireOwnedDish(id);
        dishMapper.deleteById(dish.getId());
        return Result.success();
    }

    // ---------------- 菜品分类 ----------------

    @GetMapping("/category")
    public Result<List<DishCategory>> categories(@RequestParam Long shopId) {
        requireOwnedShop(shopId);
        return Result.success(dishCategoryMapper.selectList(new LambdaQueryWrapper<DishCategory>()
                .eq(DishCategory::getShopId, shopId)
                .orderByAsc(DishCategory::getSort)));
    }

    @PostMapping("/category")
    public Result<DishCategory> createCategory(@RequestBody DishCategory category) {
        requireOwnedShop(category.getShopId());
        if (category.getSort() == null) {
            category.setSort(0);
        }
        dishCategoryMapper.insert(category);
        return Result.success(category);
    }

    private void applyFields(Dish dish, DishDTO dto) {
        dish.setShopId(dto.getShopId());
        dish.setCategoryId(dto.getCategoryId());
        dish.setName(dto.getName());
        dish.setImage(dto.getImage());
        dish.setPrice(dto.getPrice());
        dish.setOriginalPrice(dto.getOriginalPrice());
        dish.setDescription(dto.getDescription());
        dish.setStock(dto.getStock() == null ? 0 : dto.getStock());
        dish.setIsRecommend(dto.getIsRecommend() == null ? 0 : dto.getIsRecommend());
    }

    private void requireOwnedShop(Long shopId) {
        Long merchantId = BaseContext.getCurrentId();
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || !shop.getMerchantId().equals(merchantId)) {
            throw new BaseException(404, "店铺不存在");
        }
    }

    private Dish requireOwnedDish(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            throw new BaseException(404, "菜品不存在");
        }
        requireOwnedShop(dish.getShopId());
        return dish;
    }
}