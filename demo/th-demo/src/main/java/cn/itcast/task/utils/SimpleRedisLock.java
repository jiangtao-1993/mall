package cn.itcast.task.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SimpleRedisLock implements RedisLock{

    private StringRedisTemplate redisTemplate;
    /**
     * 设定好锁对应的 key
     */
    private String key;
    /**
     * 存入的线程信息的前缀，防止与其它JVM中线程信息冲突
     */
    private final String ID_PREFIX = UUID.randomUUID().toString();

    public SimpleRedisLock(StringRedisTemplate redisTemplate, String key) {
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    public boolean lock(long releaseTime) {
        log.info("有人来获取锁，{}",key);
        // 获取线程信息作为值，方便判断是否是自己的锁
        String value = ID_PREFIX + Thread.currentThread().getId();
        // 尝试获取锁
        Boolean boo = redisTemplate.opsForValue().setIfAbsent(key, value, releaseTime, TimeUnit.SECONDS);
        // 判断结果
        Boolean flag = boo != null && boo;

        if (flag){
            log.info("获取锁成功,{}",key);
        }else{
            log.info("获取锁失败,{}",key);
        }
        return boo != null && boo;
    }

    public void unlock(){
        // 获取线程信息作为值，方便判断是否是自己的锁
        String value = ID_PREFIX + Thread.currentThread().getId();
        // 获取现在的锁的值
        String val = redisTemplate.opsForValue().get(key);
        // 判断是否是自己
        if(value.equals(val)) {
            log.info("是自己的锁，自己释放：{}",key);
            // 删除key即可释放锁
            redisTemplate.delete(key);
        }else{
            log.info("不是自己的锁，不能释放：{}",key);
        }
    }
}