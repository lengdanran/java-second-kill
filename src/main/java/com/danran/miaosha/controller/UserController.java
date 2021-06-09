package com.danran.miaosha.controller;


import com.danran.miaosha.MQ.MQProducer;
import com.danran.miaosha.pojo.Book;
import com.danran.miaosha.pojo.Order;
import com.danran.miaosha.pojo.OrderMessage;
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

    private final static int EX_TIME = 20;
    private final static int DEAL = 1;
    private final static int ROLL_BACK = 0;
    private static final String ORDER_SEND_SUCCESS = "<<<<===订单发送成功===>>>>";
    private static final String ORDER_SEND_FAILED = "<<<<===订单发送失败===transactionAsyncReduceStock返回为[false]=====>>>>>>>>";
    private static final String ORDER_CREATE_FAILED = "<<<<===订单消息创建失败===>>>>=====库存不足，商品已经抢光了=====>>>>>>>>";
    private static final String ORDER_NOT_GET = "<<<<===订单发送成功,但没有抢到===>>>>";

    private static final int[] RETRY_LIST = {3 * 1000, 6 * 1000, 12 * 1000, 24 * 1000};

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
                                    @RequestParam("book_id") String bookId) {
        // 检查用户和书籍的存在信息
        boolean userExists = userService.isExists(userId);
        boolean bookExists = bookService.isExists(bookId);
        if (!userExists || !bookExists) {
            return CommonReturnType.failed("userId or bookId doesn't exits.");
        }

        Book book = (Book) redisUtil.get(bookId);
        if (book == null) {
            // redis 缓存中没有该书籍的信息，从数据中拉取，并更新缓存,设置失效时间为20s
            System.out.println("redis 缓存中没有该书籍的信息，从数据中拉取，并更新缓存,设置失效时间");
            book = bookService.getBookById(bookId);
            if (book == null) throw new RuntimeException("该书籍不存在");
            if (book.getStock() == 0) return CommonReturnType.create(ORDER_CREATE_FAILED); // 库存不足
        }
        if (book.getStock() == 0) return CommonReturnType.create(ORDER_CREATE_FAILED); // 库存不足

        // 生成订单信息(Order)
        // Order order = orderService.addOrder(userId, bookId,1);
        Order order = new Order(orderIDUtil.getOrderID(), userId, bookId, 1, DEAL);

        // 同步调用线程池的submit方法
        // 拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                Book book;
                if ((book = (Book) redisUtil.get(bookId)) == null) {
                    // redis 缓存中没有该书籍的信息，从数据中拉取，并更新缓存,设置失效时间为20s
                    System.out.println("redis 缓存中没有该书籍的信息，从数据中拉取，并更新缓存,设置失效时间");
                    book = bookService.getBookById(bookId);
                    if (book == null) throw new RuntimeException("该书籍不存在");
                    if (book.getStock() == 0) return ORDER_CREATE_FAILED; // 库存不足
                    redisUtil.set(bookId, book, EX_TIME);
                }

                if (book.getStock() == 0) return ORDER_CREATE_FAILED; // 库存不足
                // 完成对应的下单事务型消息机制
                if (!mqProducer.transactionAsyncReduceStock(order.getId(), userId, bookId, book.getVersion())) {
                    return ORDER_SEND_FAILED;
                }
                return ORDER_SEND_SUCCESS;
            }
        });

        String info;
        try {
            info = (String) future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return CommonReturnType.failed("失败：future.get()方法出错");
        }

        if (info.equals(ORDER_CREATE_FAILED)) {
            return CommonReturnType.create("商品卖完了");
        } else if (info.equals(ORDER_SEND_FAILED)) {
            return CommonReturnType.create("商品卖完了");
        } else {
            for (int time_wait : RETRY_LIST) {
                Object message = OrderMessage.getMessage(order.getId());
                if (message != null) {
                    OrderMessage.deleteMessage(order.getId());
                    return CommonReturnType.create(message);
                } else {
                    try {
                        Thread.sleep(time_wait);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return CommonReturnType.create(ORDER_NOT_GET);
        }
    }

    @RequestMapping(value = "/serialize", method = {RequestMethod.POST})
    @ResponseBody
    public CommonReturnType serializeUser(@RequestParam("user_id") int userId) {
        User user = userService.getUserById(userId);
        return CommonReturnType.create(redisUtil.set(user.getName(), user, 360));
    }

}
