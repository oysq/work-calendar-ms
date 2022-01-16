package com.oysq.workcalendarms.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.oysq.workcalendarms.entity.Res;
import com.oysq.workcalendarms.entity.User;
import com.oysq.workcalendarms.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 校验用户是否存在
     */
    @PostMapping("checkUserExists")
    public Res checkUserExists(@RequestBody User user) {
        try {
            User resUser = userService.selectByUserName(user.getUserName());
            if (resUser == null) {
                return Res.success(MapUtil.of("type", "no_exists"));
            }
            return Res.success(MapUtil.of("type", "exists"));
        } catch (Exception e) {
            log.error("checkUserExists 异常", e);
            return Res.fail(e.getMessage());
        }
    }

    /**
     * 新增用户
     */
    @PostMapping("insertUser")
    public Res insertUser(@RequestBody User user) {
        try {
            userService.insertUser(user);
            return Res.success("创建成功");
        } catch (Exception e) {
            log.error("insertUser 异常", e);
            return Res.fail(e.getMessage());
        }
    }

    /**
     * 验证Token
     */
    @PostMapping("/checkToken")
    public Res checkToken(@RequestBody User user) {
        try {
            User resUser = userService.checkToken(user.getToken());
            if (null != resUser) {
                return Res.success("验证成功", resUser);
            }
            return Res.fail("验证未通过");
        } catch (Exception e) {
            log.error("checkToken 异常", e);
            return Res.fail(e.getMessage());
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("refreshToken")
    public Res refreshToken(@RequestBody User user) {
        try {
            User resUser = userService.updateToken(user);
            return Res.success(resUser);
        } catch (Exception e) {
            log.error("refreshToken 异常", e);
            return Res.fail(e.getMessage());
        }
    }

    /**
     * 更新岗位薪资
     */
    @PostMapping("updatePostSalary")
    public Res updatePostSalary(@RequestHeader(value = "C-TOKEN") String cToken, @RequestBody User user) {
        try {
            userService.checkTokenSecurity(cToken, user);
            userService.updatePostSalary(user);
            return Res.success("更新成功");
        } catch (Exception e) {
            log.error("updatePostSalary 异常", e);
            return Res.fail(e.getMessage());
        }
    }


}
