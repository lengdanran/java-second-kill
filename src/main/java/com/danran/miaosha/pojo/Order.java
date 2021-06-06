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

    private Integer bookId;

    private Integer amount;

    private static final long serialVersionUID = 1L;
}