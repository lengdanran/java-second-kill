package com.danran.miaosha.controller;


import com.danran.miaosha.Mapper.OrderMapper;
import com.danran.miaosha.pojo.Order;
import com.danran.miaosha.response.CommonReturnType;
import com.danran.miaosha.service.BookService;
import com.danran.miaosha.service.OrderService;
import com.danran.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.expression.Bools;

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
        // 减库存
        boolean reduced = bookService.reduceBook(bookId);
        // 生成订单信息(Order)
        Order order = orderService.addOrder(userId, bookId);

        return CommonReturnType.create(order);


    }

}
