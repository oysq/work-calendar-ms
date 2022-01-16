package com.oysq.workcalendarms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oysq.workcalendarms.entity.PunchRecord;
import com.oysq.workcalendarms.entity.User;
import com.oysq.workcalendarms.mapper.UserMapper;
import com.oysq.workcalendarms.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    public User checkToken(String token) {
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
                return User.builder()
                        .userId(resUser.getUserId())
                        .userName(resUser.getUserName())
                        .postSalary(resUser.getPostSalary())
                        .build();
            } else {
                log.info("【checkToken】Token过期：" + resUser.getUserName());
                return null;
            }
        } else {
            throw new RuntimeException("不存在的token");
        }
    }

    @Override
    public void checkTokenSecurity(String token, Map<String, String> param) {
        User user = this.checkToken(token);
        if (null == user) {
            throw new RuntimeException("鉴权失败");
        }
        param.put("userId", user.getUserId());
    }

    @Override
    public void checkTokenSecurity(String token, PunchRecord punchRecord) {
        User user = this.checkToken(token);
        if (null == user) {
            throw new RuntimeException("鉴权失败");
        }
        punchRecord.setUserId(user.getUserId());
    }

    @Override
    public void checkTokenSecurity(String token, User user) {
        User resUser = this.checkToken(token);
        if (null == resUser) {
            throw new RuntimeException("鉴权失败");
        }
        if(null == user) {
            user = new User();
        }
        user.setUserId(resUser.getUserId());
    }

    @Override
    public User updateToken(User user) {
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
        return User.builder()
                .userId(resUser.getUserId())
                .userName(resUser.getUserName())
                .token(token)
                .postSalary(resUser.getPostSalary())
                .build();
    }

    @Override
    public void updatePostSalary(User user) {

        if (null == user || StrUtil.isBlank(user.getUserId())) {
            throw new RuntimeException("异常参数");
        }

        if (null == user.getPostSalary()) {
            user.setPostSalary(BigDecimal.ZERO);
        } else if (user.getPostSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("岗位薪资不可小于0");
        }

        // 更新
        if (userMapper.updateById(
                User
                        .builder()
                        .userId(user.getUserId())
                        .postSalary(user.getPostSalary().setScale(2, RoundingMode.HALF_UP))
                        .build()
        ) < 0) {
            throw new RuntimeException("更新失败");
        }
    }
}
