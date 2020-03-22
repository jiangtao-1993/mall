package com.leyou.order.task;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/*
* 清除过期订单 这里使用分布式锁是考虑订单系统如果集群的话,便会出现同一个订单多次操作
* */
@Component
@Slf4j
public class ClearOutOfTimeOrder {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedissonClient redissonClient;

    final long refreshTime = 5000; //每隔5s扫描一次订单
    @Scheduled(fixedDelay = refreshTime)
    public void scanOutOfTimeOrderAndPlusStock(){

        // 创建锁对象，并制定锁的名称
        RLock lock = redissonClient.getLock("taskLock");

        // 获取锁,设置自动失效时间为30s
        boolean isLock = lock.tryLock();

        // 判断是否获取锁
        if (!isLock) {
            // 获取失败
            log.info("获取锁失败，停止定时任务");
            return;
        }
        try {
            // 执行业务
            log.info("获取锁成功，执行定时任务。");
            // 模拟任务耗时
            //调用这个方法之前，先要，获取锁，只有锁获取成功后才能执行业务
            this.orderService.clearOrder();
        } catch (Exception e) {
            log.error("任务执行异常", e);
        } finally {
            // 释放锁
            lock.unlock();
            log.info("任务执行完毕，释放锁");
        }

    }
}
