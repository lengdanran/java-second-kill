package com.danran.miaosha.utils;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * @Classname OrderIDUtil
 * @Description TODO
 * @Date 2021/6/7 14:35
 * @Created by ASUS
 */
@Component
public class OrderIDUtil {

    @Transactional
    public String getOrderID() {
        int hashCodeV = UUID.randomUUID().toString().hashCode();
        if (hashCodeV < 0) {//有可能是负数
            hashCodeV = -hashCodeV;
        }
        // 0 代表前面补充0
        // 4 代表长度为4
        // d 代表参数为正数型
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String prefix = format.format(new Date());
        return prefix + String.format("%010d", hashCodeV);
    }

    //得到32位的uuid
    public String getUUID32(){
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();

    }

    public static void main(String[] args) {
        System.out.println(new OrderIDUtil().getOrderID());
        System.out.println(new OrderIDUtil().getUUID32().toUpperCase());
    }
}
