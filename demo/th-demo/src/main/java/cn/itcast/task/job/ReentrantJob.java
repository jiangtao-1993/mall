package cn.itcast.task.job;


import cn.itcast.task.utils.RedisLock;
import cn.itcast.task.utils.ReentrantRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ReentrantJob {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private int max = 3;

    @Scheduled(cron = "0/10 * * * * ?")
    public void hello() {
        // 创建锁对象
        RedisLock lock = new ReentrantRedisLock(redisTemplate, "lock");
        // 执行任务
        runTaskWithLock(lock, 1);
    }

    private void runTaskWithLock(RedisLock lock, int count) {
        // 获取锁,设置自动失效时间为50s
        boolean isLock = lock.lock(50);
        // 判断是否获取锁
        if (!isLock) {
            // 获取失败
            log.info("{}层 获取锁失败，停止定时任务", count);
            return;
        }
        try {
            // 执行业务
            log.info("{}层 获取锁成功，执行定时任务。", count);
            Thread.sleep(500);
            if(count < max){
                //递归调用，
                runTaskWithLock(lock, count + 1);
            }
        } catch (InterruptedException e) {
            log.error("{}层 任务执行失败", count, e);
        } finally {
            // 释放锁
            lock.unlock();
            log.info("{}层 任务执行完毕，释放锁", count);
        }
    }
}