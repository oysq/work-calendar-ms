package com.oysq.workcalendarms.service;

import com.oysq.workcalendarms.entity.User;

public interface UserService {

    /**
     * 根据用户名查询
     */
    User selectByUserName(String userName);

    /**
     * 根据用户ID查询
     */
    User selectByUserId(String userId);

    /**
     * 新增用户
     */
    void insertUser(User user);

    /**
     * 校验Token
     */
    User checkToken(String token);

    /**
     * 更新Token
     */
    User updateToken(User user);

}
