package com.danran.miaosha.pojo;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Classname OrderMessage
 * @Description TODO
 * @Date 2021/6/9 12:12
 * @Created by ASUS
 */
@Data
public class OrderMessage {

    private static Map<String, Object> orderMap = new ConcurrentHashMap<>();

    public static Object getMessage(String orderId) {
        return orderMap.get(orderId);
    }

    public static void addMessage(String orderId, Object value) {
        orderMap.put(orderId, value);
    }

    public static void deleteMessage(String orderId) {
        orderMap.remove(orderId);
    }
}
