package com.oysq.workcalendarms.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.oysq.workcalendarms.entity.Res;
import com.oysq.workcalendarms.entity.TestContent;
import com.oysq.workcalendarms.entity.User;
import com.oysq.workcalendarms.mapper.TestContentMapper;
import com.oysq.workcalendarms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/checkToken")
    public Res checkToken(@RequestBody User user) {

        if (StrUtil.hasEmpty(user.getToken())) {
            return Res.fail("token不可空");
        }
        User resUser = userService.selectByToken(user.getToken());
        if (resUser != null) {
            if (resUser.getOverdueTime().compareTo(System.currentTimeMillis()) > 0) {
                return Res.success("验证成功", MapUtil.of("name", resUser.getUserName()));
            }
        }
        return Res.fail("验证失败");
    }

    @PostMapping("checkUserExists")
    public Res checkUserExists(@RequestBody User user) {
        if (StrUtil.hasEmpty(user.getUserName())) {
            return Res.fail("用户名不可空");
        }
        User resUser = userService.selectByUserName(user.getUserName());
        if (resUser == null) {
            return Res.success(MapUtil.of("type", "no_exists"));
        }
        return Res.success(MapUtil.of("type", "exists"));
    }

    @PostMapping("insertUser")
    public Res insertUser(@RequestBody User user) {
        if (StrUtil.hasEmpty(user.getUserName(), user.getPassword())) {
            return Res.fail("用户名和密码不可空");
        }
        User resUser = userService.selectByUserName(user.getUserName());
        if (resUser != null) {
            return Res.fail("用户名已存在");
        }
        userService.insertUser(
                User.builder()
                        .userId(UUID.randomUUID().toString().replaceAll("-", ""))
                        .userName(user.getUserName())
                        .password(user.getPassword())
                        .createTime(DateUtil.formatTime(new Date()))
                        .build()
        );
        return Res.success("创建成功");
    }

    @PostMapping("refreshToken")
    public Res refreshToken(@RequestBody User user) {
        if (StrUtil.hasEmpty(user.getUserName(), user.getPassword())) {
            return Res.fail("用户名和密码不可空");
        }
        User resUser = userService.selectByUserName(user.getUserName());
        if (resUser == null || !resUser.getPassword().equals(user.getPassword())) {
            return Res.fail("用户名或密码错误");
        }
        String token = userService.updateToken(resUser.getUserId());
        return Res.success(MapUtil.of("token", token));
    }


}
