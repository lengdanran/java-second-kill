package com.danran.miaosha.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * book
 * @author 
 */
@Data
public class Book implements Serializable {
    private Integer id;

    private String bookName;

    private Integer stock;

    private Integer version;

    private static final long serialVersionUID = 1L;
}