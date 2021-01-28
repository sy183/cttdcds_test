package com.suy.mapper;

import com.suy.config.DaoConfig;
import com.suy.pojo.User;
import com.suy.util.GsonUtil;
import com.suy.util.LoggerUtil;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class UserMapperTest {
    @Test
    public void test() {
        ApplicationContext context = new AnnotationConfigApplicationContext(DaoConfig.class);
        UserMapper userMapper = (UserMapper) context.getBean("userMapper");
        List<User> users = userMapper.getUsers();
        LoggerUtil.logger.info(GsonUtil.gson.toJson(users));
    }
}
