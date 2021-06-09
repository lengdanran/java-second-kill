package com.danran.miaosha.service;

import com.danran.miaosha.Mapper.OrderMapper;
import com.danran.miaosha.pojo.Order;
import com.danran.miaosha.utils.OrderIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Classname OrderService
 * @Description TODO
 * @Date 2021/6/6 12:17
 * @Created by LengDanran
 */
@Service
public class OrderService {

    private final static int DEAL = 1;
    private final static int ROLL_BACK = 0;

    @Autowired
    private OrderMapper orderMapper;


    @Transactional
    public Order addOrder(String orderID, int userId, String bookId, int amount) {
        Order order = new Order(orderID, userId, bookId, amount, DEAL);
        System.out.println("<<<<<<<<<=================添加order=================>>>>>>>>>>");
        orderMapper.insert(order);
        return order;
    }

    @Transactional
    public Order rollbackOrder(String orderID) {
        Order order = orderMapper.selectByPrimaryKey(orderID);
        order.setStatus(ROLL_BACK);
        System.out.println("<<<<<<<<<=================order回滚=================>>>>>>>>>>");
        orderMapper.insertSelective(order);
        return order;
    }

//    @Transactional
    public Order getOrderById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }
}
