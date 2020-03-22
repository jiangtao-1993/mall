package com.leyou.common.auth.entity;

import lombok.Data;

import java.util.Date;


@Data
public class Payload<T> {
    private String id; //tokenId
    private T info; //实际存放内容
    private Date expiration; //过期时间
}