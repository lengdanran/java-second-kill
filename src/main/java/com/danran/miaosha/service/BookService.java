package com.danran.miaosha.service;

import com.danran.miaosha.Mapper.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Classname BookService
 * @Description TODO
 * @Date 2021/6/6 11:15
 * @Created by ASUS
 */
@Service
public class BookService {

    @Autowired
    private BookMapper bookMapper;

    public boolean isExists(int bookId) {
        return bookMapper.selectByPrimaryKey(bookId) != null;
    }

    public boolean reduceBook(int bookId) {
        return bookMapper.reduceBook(bookId) == 1;
    }
}
