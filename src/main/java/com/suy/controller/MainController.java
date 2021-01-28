package com.suy.controller;

import com.suy.pojo.User;
import com.suy.service.UserService;
import com.suy.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MainController {
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/get/addressBook")
    public String getAddressBook() {
        return GsonUtil.gson.toJson(userService.getUsers());
    }

    @PostMapping("/insert/user")
    public String insertUser(User user) {
        Map<String, Object> result = new HashMap<>();

        int ret = userService.insertUser(user);
        if (ret < 1) {
            result.put("result", false);
        } else {
            result.put("result", true);
        }
        switch (ret) {
            case 0 -> result.put("cause", "数据库插入失败");
            case -4 -> result.put("cause", "用户名重复");
            case -5 -> result.put("cause", "未指定用户名");
            case -255 -> result.put("cause", "服务器错误");
        }
        return GsonUtil.gson.toJson(result);
    }

    @PostMapping("delete/user/{id:\\d+}")
    public String deleteUserById(@PathVariable("id") Integer id) {
        Map<String, Object> result = new HashMap<>();

        int ret = userService.deleteUserById(id);
        if (ret < 1) {
            result.put("result", false);
        } else {
            result.put("result", true);
        }
        switch (ret) {
            case 0 -> result.put("cause", "数据库插入失败");
            case -1 -> result.put("cause", "指定用户不存在");
            case -255 -> result.put("cause", "服务器错误");
        }
        return GsonUtil.gson.toJson(result);
    }

    @PostMapping("update/user")
    public String updateUser(User user) {
        Map<String, Object> result = new HashMap<>();

        int ret = userService.updateUser(user);
        if (ret < 1) {
            result.put("result", false);
        } else {
            result.put("result", true);
        }
        switch (ret) {
            case 0 -> result.put("cause", "数据库插入失败");
            case -1 -> result.put("cause", "指定用户不存在");
            case -2 -> result.put("cause", "未指定用户id");
            case -3 -> result.put("cause", "未指定修改项");
            case -4 -> result.put("cause", "用户名重复");
            case -255 -> result.put("cause", "服务器错误");
        }
        return GsonUtil.gson.toJson(result);
    }
}
