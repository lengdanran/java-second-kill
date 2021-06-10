package com.danran.miaosha.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * book
 * @author 
 */
@Data
public class Book implements Serializable {
    private String id;

    private String bookName;

    private Integer stock;

    private Integer version;

    private static final long serialVersionUID = 1L;

    public void reduceStack() {
        this.stock = this.stock - 1;
        this.version = this.version + 1;
    }
}