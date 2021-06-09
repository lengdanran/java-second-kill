package com.danran.miaosha.pojo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * order
 * @author 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable {
    private String id;

    private Integer userId;

    private String bookId;

    private Integer amount;

    private Integer status;

    private static final long serialVersionUID = 1L;
}