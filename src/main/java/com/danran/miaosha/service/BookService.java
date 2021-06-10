package com.danran.miaosha.service;

import com.danran.miaosha.Mapper.BookMapper;
import com.danran.miaosha.pojo.Book;
import com.danran.miaosha.utils.MyRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    @Autowired
    private MyRedisUtil redisUtil;



    @Value("${book.alive}")
    private int BOOK_ALIVE;

    public boolean isExists(String bookId) {
        Book book = (Book) redisUtil.get(bookId);
        if (book == null) {
            System.out.println("<<<===Redis中没有该书籍的信息，从数据库拉取，并刷新缓存===>>>存活时间为："+BOOK_ALIVE+"s");
            book = bookMapper.selectByPrimaryKey(bookId);
            if (book == null) return false;
            redisUtil.set(bookId, book, BOOK_ALIVE);
        }
        return bookMapper.selectByPrimaryKey(bookId) != null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Book> getBookInRedis() {
          return  (Map<String, Book>) redisUtil.get("book_map");
    }

    public List<Book> getAllBook() {
        return bookMapper.getAllBook();
    }

    public Map<String, Book> getAllBookMap() {
        List<Book> allBook = getAllBook();
        Map<String, Book> map = new ConcurrentHashMap<>();
        for (Book book : allBook) {
            map.put(book.getId(), book);
        }
        return map;
    }

    public boolean reduceBook(String bookId, int version) {
        synchronized (this) {
            Book book = bookMapper.selectByPrimaryKey(bookId);
            if (book.getStock() == 0) return false;
            if (version != book.getVersion()) return false;
            return bookMapper.reduceBook(bookId) == 1;
        }
    }

    public void updateBook(Book book) {
        bookMapper.updateByPrimaryKeySelective(book);
    }

    public Book getBookById(String bookId) {
        return bookMapper.selectByPrimaryKey(bookId);
    }
}
