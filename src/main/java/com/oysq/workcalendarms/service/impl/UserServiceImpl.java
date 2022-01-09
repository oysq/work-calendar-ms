package com.oysq.workcalendarms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oysq.workcalendarms.entity.User;
import com.oysq.workcalendarms.mapper.UserMapper;
import com.oysq.workcalendarms.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User selectByUserName(String userName) {
        if (StrUtil.isBlank(userName)) {
            throw new RuntimeException("用户名不可空");
        }
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
    public User selectByUserId(String userId) {
        if (StrUtil.isBlank(userId)) {
            throw new RuntimeException("用户ID不可空");
        }
        return userMapper.selectById(userId);
    }

    @Override
    public void insertUser(User user) {
        if (StrUtil.hasBlank(user.getUserName(), user.getPassword())) {
            throw new RuntimeException("用户名和密码不可空");
        }
        User resUser = this.selectByUserName(user.getUserName());
        if (resUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        if (userMapper.insert(
                User.builder()
                        .userId(UUID.randomUUID().toString().replaceAll("-", ""))
                        .userName(user.getUserName())
                        .password(user.getPassword())
                        .createTime(DateUtil.formatTime(new Date()))
                        .build()
        ) <= 0) {
            throw new RuntimeException("数据入库失败");
        }
    }

    @Override
    public String checkToken(String token) {
        if (StrUtil.isBlank(token)) {
            throw new RuntimeException("token不可空");
        }
        List<User> userList = userMapper.selectList(
                new QueryWrapper<User>().eq("token", token)
        );
        if (CollUtil.isNotEmpty(userList)) {
            if (userList.size() > 1) {
                throw new RuntimeException("存在相同的token");
            }
            User resUser = userList.get(0);
            if (resUser.getOverdueTime().compareTo(System.currentTimeMillis()) > 0) {
                log.info("【checkToken】验证通过：" + resUser.getUserName());
                return resUser.getUserName();
            } else {
                log.info("【checkToken】Token过期：" + resUser.getUserName());
                return "";
            }
        } else {
            throw new RuntimeException("不存在的token");
        }
    }

    @Override
    public String updateToken(User user) {
        if (StrUtil.hasEmpty(user.getUserName(), user.getPassword())) {
            throw new RuntimeException("用户名和密码不可空");
        }
        User resUser = this.selectByUserName(user.getUserName());
        if (resUser == null || !resUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = UUID.randomUUID().toString().replaceAll("-", "");
        long buildTime = System.currentTimeMillis();
        long overdueTime = buildTime + (7 * 24 * 60 * 60 * 1000);
        userMapper.updateById(
                User.builder().userId(resUser.getUserId()).token(token).buildTime(buildTime).overdueTime(overdueTime).build()
        );
        return token;
    }
}
