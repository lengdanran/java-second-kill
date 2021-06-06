package com.danran.miaosha.service;


import com.danran.miaosha.Mapper.BookMapper;
import com.danran.miaosha.Mapper.UserMapper;
import com.danran.miaosha.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Classname UserService
 * @Description TODO
 * @Date 2021/6/6 10:34
 * @Created by ASUS
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public boolean isExists(int userId) {
        return userMapper.selectByPrimaryKey(userId) != null;
    }

}
