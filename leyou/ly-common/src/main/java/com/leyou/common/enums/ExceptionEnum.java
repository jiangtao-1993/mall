package com.leyou.common.enums;

import lombok.Getter;

@Getter
public enum ExceptionEnum {
    PRICE_CANNOT_BE_NULL(400, "价格不能为空！"),
    CATEGORY_NOT_FOUND(204,"对应分类信息不存在"),
    BRAND_NOT_FOUND(204,"品牌对应信息不存在"),
    DATA_NOT_FOUND(204,"对应信息不存在"),
    DATA_TRANSFER_ERROR(500,"通用消息转换失败"),
    BRAND_SAVE_ERROR(500,"品牌保存失败"),
    INVALID_REQUEST_PARAM(400,"前台提供的请求参数异常"),
    FILE_SAVE_ERROR(500,"服务端文件上传失败"),
    DATA_MODIFY_ERROR(500,"服务端数据修改失败"),
    DATA_SERVER_OPERATION_ERROR(500,"数据库服务操作异常"),
    DATA_SAVE_ERROR(500,"后台保存数据失败"),
    DIRECTORY_WRITER_ERROR(500,"文件目录创建失败"),
    FILE_WRITER_ERROR(500,"文件输出失败"),
    SEND_MESSAGE_ERROR(500,"服务端短信发送失败"),
    USER_SERVICE_ERROR(500,"用户服务不可用"),
    INVALID_TOKEN_COOKIE(401,"请求未授权"),
    CART_IS_NULL(400,"此用户购物车数据为空"),
    CART_NOT_FOUND(400,"对应购物车数据不存在"),
    STOCK_MINUS_ERROR(500,"库存不足"),
    INVALID_NOTIFY_SIGN(400,"微信回调给的签名有误"),
    INVALID_NOTIFY_PARAM(400,"微信回调给的参数有误")
    ;
    private int status;
    private String message;

    ExceptionEnum(int status, String message) {
        this.status = status;
        this.message = message;
    }
}