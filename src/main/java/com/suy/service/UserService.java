package com.suy.service;

import com.suy.pojo.User;

import java.util.List;

public interface UserService {
    List<User> getUsers();
    int insertUser(User user);
    int deleteUserById(Integer id);
    int updateUser(User user);
}
