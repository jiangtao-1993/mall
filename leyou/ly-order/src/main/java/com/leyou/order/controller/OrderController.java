package com.leyou.order.controller;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.entity.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @PostMapping
    public ResponseEntity<Long> createOrder(
            @RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok(this.orderService.createOrder(orderDTO));
    }

    @GetMapping("{orderId}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("orderId")Long orderId){
        return ResponseEntity.ok(this.orderService.queryOrderById(orderId));
    }

    @GetMapping("url/{orderId}")
    public ResponseEntity<String> generatePayUrl(@PathVariable("orderId")Long orderId){

        return ResponseEntity.ok(this.orderService.generatePayUrl(orderId));
    }


    /**
     * 根据订单id查询其状态
     * @param orderId
     * @return
     */
    @GetMapping("state/{orderId}")
    public ResponseEntity<Integer> queryOrderStateById(@PathVariable("orderId")Long orderId){
        return ResponseEntity.ok(this.orderService.queryOrderStateById(orderId));
    }


}
