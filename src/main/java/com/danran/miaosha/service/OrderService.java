package com.danran.miaosha.service;

import com.danran.miaosha.Mapper.OrderMapper;
import com.danran.miaosha.pojo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Classname OrderService
 * @Description TODO
 * @Date 2021/6/6 12:17
 * @Created by LengDanran
 */
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    public Order addOrder(int userId, int bookId) {
        Order order = new Order("test_id", userId, bookId, 1);
        orderMapper.insert(order);
        return order;
    }

}
