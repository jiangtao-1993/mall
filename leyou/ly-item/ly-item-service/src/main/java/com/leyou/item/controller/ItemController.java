package com.leyou.item.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.pojo.Item;
import com.leyou.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping("item")
    public ResponseEntity<Item> saveItem(Item item){
        // 如果价格为空，则抛出异常，返回400状态码，请求参数有误
        if(item.getPrice() == null){
            throw new LyException(ExceptionEnum.PRICE_CANNOT_BE_NULL);
        }
        Item result = itemService.saveItem(item);
        //201,item
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
