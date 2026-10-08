package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.constant.JwtClaimsConstant;
import com.dianping.common.constant.StatusConstant;
import com.dianping.common.exception.BaseException;
import com.dianping.common.properties.JwtProperties;
import com.dianping.common.utils.JwtUtil;
import com.dianping.dto.UserLoginDTO;
import com.dianping.entity.User;
import com.dianping.mapper.UserMapper;
import com.dianping.service.UserService;
import com.dianping.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        // 骨架阶段演示：真实环境应调用微信 code2session 接口换取 openid
        String openid = "mock-openid-" + userLoginDTO.getCode();

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickName(userLoginDTO.getNickName() == null ? "新用户" : userLoginDTO.getNickName());
            user.setIcon(userLoginDTO.getAvatar());
            user.setStatus(StatusConstant.ENABLE);
            userMapper.insert(user);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        return UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .nickName(user.getNickName())
                .icon(user.getIcon())
                .build();
    }
}