package cn.itcast.task.job;

import cn.itcast.task.utils.RedisLock;
import cn.itcast.task.utils.SimpleRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class HelloJob {

    @Autowired
    private StringRedisTemplate redisTemplate;

    //@Scheduled(cron = "0/2 * * * * ?")
    public void hello() {
        // 创建锁对象
        RedisLock lock = new SimpleRedisLock(redisTemplate, "lock");
        // 获取锁,设置自动失效时间为50s
        boolean isLock = lock.lock(3);
        // 判断是否获取锁
        if (!isLock) {
            // 获取失败
            //log.info("获取锁失败，停止定时任务");
            return;
        }
        try {
            // 执行业务
            log.info("获取锁成功，执行定时任务。");
            hello();
            // 模拟任务耗时
            Thread.sleep(1);
        } catch (InterruptedException e) {
            log.error("任务执行异常", e);
        } finally {
            log.info("最终释放锁");
            // 释放锁
            //lock.unlock();
        }
    }
}