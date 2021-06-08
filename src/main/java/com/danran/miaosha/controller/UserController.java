package com.danran.miaosha.controller;


import com.danran.miaosha.MQ.MQProducer;
import com.danran.miaosha.pojo.Order;
import com.danran.miaosha.pojo.User;
import com.danran.miaosha.response.CommonReturnType;
import com.danran.miaosha.service.BookService;
import com.danran.miaosha.service.OrderService;
import com.danran.miaosha.service.UserService;
import com.danran.miaosha.utils.OrderIDUtil;
import com.danran.miaosha.utils.RedisUtil;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

/**
 * @Classname UserController
 * @Description TODO
 * @Date 2021/6/6 9:36
 * @Created by LengDanran
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MQProducer mqProducer;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderIDUtil orderIDUtil;

    private ExecutorService executorService;
    private RateLimiter orderCreateRateLimiter;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(20);
        orderCreateRateLimiter = RateLimiter.create(300);
    }

    @RequestMapping(value = "/getBook", method = {RequestMethod.POST})
    @ResponseBody
    public CommonReturnType getBook(@RequestParam("user_id") int userId,
                                    @RequestParam("book_id") int bookId) {
        // 检查用户和书籍的存在信息
        boolean userExists = userService.isExists(userId);
        boolean bookExists = bookService.isExists(bookId);
        if (!userExists || !bookExists) {
            return CommonReturnType.failed("userId or bookId doesn't exits.");
        }
        // 生成订单信息(Order)
        // Order order = orderService.addOrder(userId, bookId,1);
        Order order = new Order(orderIDUtil.getOrderID(), userId, bookId, 1);

        // 同步调用线程池的submit方法
        // 拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {

                // 完成对应的下单事务型消息机制
                if (!mqProducer.transactionAsyncReduceStock(order.getId(), userId, bookId)) {
                    throw new Exception("下单失败");
                }
                return "<<<<===下单成功===>>>>";
            }
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return CommonReturnType.failed("失败：future.get()方法出错");
        }

        return CommonReturnType.create(order);

    }

    @RequestMapping(value = "/serialize", method = {RequestMethod.POST})
    @ResponseBody
    public CommonReturnType serializeUser(@RequestParam("user_id") int userId) {
        User user = userService.getUserById(userId);
        return CommonReturnType.create(redisUtil.set(user.getName(), user, 360));
    }

}
