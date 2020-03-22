package com.leyou.cart.controller;

import com.leyou.cart.entity.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){


        this.cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户信息查询其对应的购物车内容
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCarts(){
        return ResponseEntity.ok(this.cartService.queryCarts());
    }

    @PutMapping
    public ResponseEntity<Void> modifyCartNum(
            @RequestParam("id")Long id,
            @RequestParam("num")Integer num){

        this.cartService.modifyCartNum(id,num);
        return ResponseEntity.ok().build();
    }

    @PostMapping("list")
    public ResponseEntity<Void> mergeCart(@RequestBody List<Cart> carts){

        this.cartService.mergeCart(carts);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
