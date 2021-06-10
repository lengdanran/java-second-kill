package com.danran.miaosha.service;


import com.danran.miaosha.Mapper.BookMapper;
import com.danran.miaosha.Mapper.UserMapper;
import com.danran.miaosha.pojo.User;
import com.danran.miaosha.utils.MyRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Autowired
    private MyRedisUtil redisUtil;

    @Value("${user.alive}")
    private int USER_ALIVE;

    public boolean isExists(int userId) {
        User user = (User) redisUtil.get(String.valueOf(userId));
        if(user == null){
            System.out.println("<<<===Redis中没有该用户的信息，从数据库拉取，并刷新缓存,存活时间为：" + USER_ALIVE + " s ===>>>");
            user = userMapper.selectByPrimaryKey(userId);
            if (user == null) return false;
            redisUtil.set(String.valueOf(user.getId()), user, USER_ALIVE);
        }
        return true;
    }

    public User getUserById(int userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

}
