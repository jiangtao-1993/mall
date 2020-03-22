package com.leyou.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    //支付类型
    private Integer paymentType;
    //购物车集合
    private List<CartDTO> carts;
    //地址ID
    private Long addressId;
}
