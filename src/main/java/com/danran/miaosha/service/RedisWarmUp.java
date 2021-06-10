package com.danran.miaosha.service;

import com.danran.miaosha.pojo.Book;
import com.danran.miaosha.utils.MyRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Classname RedisWarmUp
 * @Description TODO
 * @Date 2021/6/10 11:23
 * @Created by LengDanran
 */
@Component
public class RedisWarmUp  {

    @Autowired
    private MyRedisUtil redisUtil;

    @Autowired
    private BookService bookService;

    public synchronized void warmUp() {
        System.out.println("<<==开始缓存数据预热==>>");
        Map<String, Book> allBookMap = bookService.getAllBookMap();
        System.out.println(allBookMap);
        redisUtil.set("book_map", allBookMap);
        System.out.println("<<==缓存数据预热成功==>>");
    }

//    @Override
    public void run(ApplicationArguments args) {
        warmUp();
    }
}
