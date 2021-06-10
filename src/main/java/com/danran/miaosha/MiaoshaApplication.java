package com.danran.miaosha;

import com.danran.miaosha.service.RedisWarmUp;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.danran.miaosha.Mapper")
//@EnableScheduling
public class MiaoshaApplication {


    public static void main(String[] args) {
        SpringApplication.run(MiaoshaApplication.class, args);
    }



}
