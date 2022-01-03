package com.oysq.workcalendarms.service;

import com.oysq.workcalendarms.entity.User;

public interface UserService {

    User selectByUserName(String userName);

    User selectByToken(String token);

    void insertUser(User user);

    String updateToken(String userId);

}
