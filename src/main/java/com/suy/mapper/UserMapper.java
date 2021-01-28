package com.suy.mapper;

import com.suy.pojo.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {
    List<User> getUsers();
    User getUserById(Integer id);
    User getUserByName(String name);
    int insertUser(User user);
    int deleteUserById(Integer id);
    int updateUser(User user);
}
