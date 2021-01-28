package com.suy.service;

import com.suy.mapper.UserMapper;
import com.suy.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 增删改返回值对应原因：
 * 1. 成功，返回1
 * 2. 数据库层面增删改失败，返回0
 * 3. 删除或改动用户不存在，返回-1
 * 4. 改动未指定用户，返回-2
 * 5. 未指定改动项，返回-3
 * 6. 插入或改动时用户名重复，返回-4
 * 7. 插入时未指定用户名，返回-5
 * 8. 代码逻辑错误，返回-255
 */
@Service
public class UserServiceImpl implements UserService {
    private UserMapper userMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<User> getUsers() {
        return userMapper.getUsers();
    }

    @Override
    public int insertUser(User user) {
        if (user == null) {
            return 0;
        }
        if (user.getName() == null) {
            return -5;
        }
        if (userMapper.getUserByName(user.getName()) != null) {
            return -4;
        }
        return userMapper.insertUser(user);
    }

    @Override
    public int deleteUserById(Integer id) {
        if (id == null) {
            return -255;
        }
        if (userMapper.getUserById(id) == null) {
            return -1;
        }
        return userMapper.deleteUserById(id);
    }

    @Override
    public int updateUser(User user) {
        User tempUser;

        if (user == null) {
            return -255;
        }
        if (user.getId() == null) {
            return -2;
        }
        if (user.getName() == null && user.getNumber() == null && user.getServer() == null) {
            return -3;
        }
        if (user.getName() != null) {
            if ((tempUser = userMapper.getUserByName(user.getName())) != null) {
                if (!tempUser.getId().equals(user.getId())) {
                    return -4;
                }
            }
        }
        if (userMapper.getUserById(user.getId()) == null) {
            return -1;
        }
        return userMapper.updateUser(user);
    }
}
