package com.oysq.workcalendarms.service;

import com.oysq.workcalendarms.entity.PunchRecord;
import com.oysq.workcalendarms.entity.User;

import java.util.Map;

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
     * token失败直接抛出异常，用于鉴权
     */
    void checkTokenSecurity(String token, Map<String, String> param);
    void checkTokenSecurity(String token, PunchRecord punchRecord);
    void checkTokenSecurity(String token, User user);

    /**
     * 更新Token
     */
    User updateToken(User user);

    /**
     * 更新岗位薪资
     */
    void updatePostSalary(User user);

}
