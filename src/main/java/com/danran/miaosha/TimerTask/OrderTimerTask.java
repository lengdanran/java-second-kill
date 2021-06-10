package com.danran.miaosha.TimerTask;

import com.danran.miaosha.pojo.Order;
import com.danran.miaosha.service.OrderService;
import com.danran.miaosha.utils.MyRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Classname OrderTimerTask
 * @Description TODO
 * @Date 2021/6/10 13:29
 * @Created by LengDanran
 */
@Component
public class OrderTimerTask {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MyRedisUtil redisUtil;

    @Async
    @Scheduled(fixedDelay = 120 * 1000)
    @SuppressWarnings("unchecked")
    public void taskStart() {
        System.out.println("<<<<===开始订单定时任务===>>>>");
        List<Order> orderList = (List<Order>) redisUtil.get("order_list");
        if (orderList == null) return;
        synchronized (this) {
            for (Order order : orderList) {
                orderService.insertOrder(order);
            }
            System.out.println("<<<<====更新 " + orderList.size() + " 条订单信息到数据库====>>>>");
            orderList.clear();
            redisUtil.set("order_list", orderList);
        }
    }
}
