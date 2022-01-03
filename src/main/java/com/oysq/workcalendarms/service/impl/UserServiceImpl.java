package com.oysq.workcalendarms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oysq.workcalendarms.entity.User;
import com.oysq.workcalendarms.mapper.UserMapper;
import com.oysq.workcalendarms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User selectByUserName(String userName) {
        List<User> userList = userMapper.selectList(
                new QueryWrapper<User>().eq("user_name", userName)
        );
        if (CollUtil.isNotEmpty(userList)) {
            if (userList.size() > 1) {
                throw new RuntimeException("存在同名用户");
            }
            return userList.get(0);
        }
        return null;
    }

    @Override
    public User selectByToken(String token) {
        List<User> userList = userMapper.selectList(
                new QueryWrapper<User>().eq("token", token)
        );
        if (CollUtil.isNotEmpty(userList)) {
            if (userList.size() > 1) {
                throw new RuntimeException("存在相同的token");
            }
            return userList.get(0);
        }
        return null;
    }

    @Override
    public void insertUser(User user) {
        userMapper.insert(user);
    }

    @Override
    public String updateToken(String userId) {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        long buildTime = System.currentTimeMillis();
        long overdueTime = buildTime + (7 * 24 * 60 * 60 * 1000);
        userMapper.updateById(
                User.builder().userId(userId).token(token).buildTime(buildTime).overdueTime(overdueTime).build()
        );
        return token;
    }
}
