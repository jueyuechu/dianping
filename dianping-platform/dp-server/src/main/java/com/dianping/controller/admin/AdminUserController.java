package com.dianping.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.context.BaseContext;
import com.dianping.common.result.Result;
import com.dianping.dto.UserBanDTO;
import com.dianping.entity.User;
import com.dianping.entity.UserBan;
import com.dianping.mapper.UserBanMapper;
import com.dianping.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 管理员端用户管理：列表、封禁（分级+时长）、解封
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBanMapper userBanMapper;

    @GetMapping("/list")
    public Result<Page<User>> list(@RequestParam(defaultValue = "1") long current,
                                   @RequestParam(defaultValue = "10") long size,
                                   @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(keyword != null && !keyword.isBlank(), User::getNickName, keyword)
                .orderByDesc(User::getCreateTime);
        return Result.success(userMapper.selectPage(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(userMapper.selectById(id));
    }

    /**
     * 封禁（分级 + 时长，0 为永久）
     */
    @PostMapping("/{id}/ban")
    public Result<Void> ban(@PathVariable Long id, @RequestBody @Valid UserBanDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        UserBan ban = new UserBan();
        ban.setUserId(id);
        ban.setBanType(dto.getBanType());
        ban.setReason(dto.getReason());
        ban.setStartTime(LocalDateTime.now());
        ban.setEndTime(dto.getDuration() == null || dto.getDuration() == 0
                ? null : LocalDateTime.now().plusDays(dto.getDuration()));
        ban.setStatus(0);
        ban.setOperatorId(BaseContext.getCurrentId());
        userBanMapper.insert(ban);
        // 禁登录时同步禁用账号
        if (dto.getBanType() == 1) {
            user.setStatus(0);
            userMapper.updateById(user);
        }
        return Result.success();
    }

    /**
     * 解封（解除该用户所有生效封禁）
     */
    @PostMapping("/{id}/unban")
    public Result<Void> unban(@PathVariable Long id) {
        userBanMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<UserBan>()
                .eq(UserBan::getUserId, id)
                .eq(UserBan::getStatus, 0)
                .set(UserBan::getStatus, 1));
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(1);
            userMapper.updateById(user);
        }
        return Result.success();
    }

    /**
     * 封禁记录
     */
    @GetMapping("/ban-records")
    public Result<Page<UserBan>> banRecords(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) Long userId) {
        Page<UserBan> page = new Page<>(current, size);
        return Result.success(userBanMapper.selectPage(page, new LambdaQueryWrapper<UserBan>()
                .eq(userId != null, UserBan::getUserId, userId)
                .orderByDesc(UserBan::getCreateTime)));
    }
}