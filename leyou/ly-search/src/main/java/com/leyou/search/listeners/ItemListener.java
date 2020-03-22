package com.leyou.search.listeners;

import com.leyou.search.service.IndexService;
import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.MQConstants.Exchange.*;
import static com.leyou.common.constants.MQConstants.Queue.*;
import static com.leyou.common.constants.MQConstants.RoutingKey.*;

@Component
public class ItemListener {

    @Autowired
    private IndexService indexService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SEARCH_ITEM_UP, durable = "true"),
            exchange = @Exchange(
                    name = ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = ITEM_UP_KEY
    ))
    public void listenInsert(Long id){
        if(id != null){
            // 新增索引
            indexService.createIndex(id);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SEARCH_ITEM_DOWN, durable = "true"),
            exchange = @Exchange(
                    name = ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = ITEM_DOWN_KEY
    ))
    public void listenDelete(Long id){
        if(id != null){
            // 删除
            indexService.deleteById(id);
        }
    }
}