package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BaseException;
import com.dianping.entity.UserBan;
import com.dianping.mapper.UserBanMapper;
import com.dianping.service.UserBanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserBanServiceImpl implements UserBanService {

    @Autowired
    private UserBanMapper userBanMapper;

    @Override
    public void assertAllowed(Long userId, int banType) {
        if (userId == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        UserBan ban = userBanMapper.selectOne(new LambdaQueryWrapper<UserBan>()
                .eq(UserBan::getUserId, userId)
                .eq(UserBan::getBanType, banType)
                .eq(UserBan::getStatus, 0)
                .and(w -> w.isNull(UserBan::getStartTime).or().le(UserBan::getStartTime, now))
                .and(w -> w.isNull(UserBan::getEndTime).or().gt(UserBan::getEndTime, now))
                .orderByDesc(UserBan::getCreateTime)
                .last("LIMIT 1"));
        if (ban != null) {
            String reason = ban.getReason() == null || ban.getReason().isBlank()
                    ? "违反平台规则" : ban.getReason();
            throw new BaseException("账号已被限制：" + reason);
        }
    }
}
