package com.dianping.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.result.Result;
import com.dianping.entity.Admin;
import com.dianping.entity.Role;
import com.dianping.mapper.AdminMapper;
import com.dianping.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员端系统设置：账号、角色（骨架阶段只读，新增/编辑 Phase 1 补全）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminAccountController {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RoleMapper roleMapper;

    @GetMapping("/account/list")
    public Result<Page<Admin>> accountList(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size) {
        Page<Admin> page = new Page<>(current, size);
        // 查询时排除密码字段
        page = adminMapper.selectPage(page, new LambdaQueryWrapper<Admin>()
                .select(Admin.class, f -> !"password".equals(f.getProperty()))
                .orderByDesc(Admin::getCreateTime));
        return Result.success(page);
    }

    @GetMapping("/role/list")
    public Result<List<Role>> roleList() {
        return Result.success(roleMapper.selectList(new LambdaQueryWrapper<Role>().orderByAsc(Role::getSort)));
    }
}