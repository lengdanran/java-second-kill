package com.danran.miaosha.service;

import com.danran.miaosha.Mapper.BookMapper;
import com.danran.miaosha.pojo.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public boolean isExists(String bookId) {
        return bookMapper.selectByPrimaryKey(bookId) != null;
    }

    @Transactional
    public boolean reduceBook(String bookId, int version) {
        synchronized (this) {
            Book book = bookMapper.selectByPrimaryKey(bookId);
            if (book.getStock() == 0) return false;
            if (version > book.getVersion()) return false;
            return bookMapper.reduceBook(bookId) == 1;
        }
    }

    public Book getBookById(String bookId) {
        return bookMapper.selectByPrimaryKey(bookId);
    }
}
