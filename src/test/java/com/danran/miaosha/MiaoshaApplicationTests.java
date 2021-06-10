package com.danran.miaosha;

import com.danran.miaosha.Mapper.BookMapper;
import com.danran.miaosha.pojo.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MiaoshaApplicationTests {

    @Autowired
    private BookMapper bookMapper;

    @Test
    void contextLoads() {
        List<Book> allBook = bookMapper.getAllBook();
        System.out.println(allBook);
    }

}
