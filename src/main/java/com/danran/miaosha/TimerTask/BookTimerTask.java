package com.danran.miaosha.TimerTask;

import com.danran.miaosha.pojo.Book;
import com.danran.miaosha.service.BookService;
import com.danran.miaosha.utils.MyRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Classname BookTimerTask
 * @Description TODO
 * @Date 2021/6/10 13:28
 * @Created by LengDanran
 */
@Component
public class BookTimerTask {

    @Autowired
    private BookService bookService;

    @Autowired
    private MyRedisUtil redisUtil;

    @Async
    @Scheduled(fixedDelay = 60 * 1000)
    @SuppressWarnings("unchecked")
    public void taskStart() {
        System.out.println("<<<<====开始刷新商品任务====>>>>");
        Map<String, Book> bookMap = (Map<String, Book>) redisUtil.get("book_map");
        if (bookMap == null) return;
        for (Book book : bookMap.values()) {
            bookService.updateBook(book);
        }
        System.out.println("<<<<====更新商品信息到数据库====>>>>");
    }

}
