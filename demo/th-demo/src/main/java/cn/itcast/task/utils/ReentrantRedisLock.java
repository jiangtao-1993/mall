package cn.itcast.task.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;
import java.util.UUID;

public class ReentrantRedisLock implements RedisLock {

    private StringRedisTemplate redisTemplate;
    /**
     * 设定好锁对应的 key
     */
    private String key;

    /**
     * 存入的线程信息的前缀，防止与其它JVM中线程信息冲突
     */
    private final String ID_PREFIX = UUID.randomUUID().toString();

    public ReentrantRedisLock(StringRedisTemplate redisTemplate, String key) {
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    private static final DefaultRedisScript<Long> LOCK_SCRIPT;
    private static final DefaultRedisScript<Object> UNLOCK_SCRIPT;
    static {
        // 加载获取锁的脚本
        LOCK_SCRIPT = new DefaultRedisScript<>();
        LOCK_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("lock.lua")));
        LOCK_SCRIPT.setResultType(Long.class);

        // 加载释放锁的脚本
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("unlock.lua")));
    }
    // 锁释放时间
    private String releaseTime;

    @Override
    public boolean lock(long releaseTime) {
        // 记录释放时间
        this.releaseTime = String.valueOf(releaseTime);
        // 执行脚本
        Long result = redisTemplate.execute(
                LOCK_SCRIPT,
                Collections.singletonList(key),
                ID_PREFIX + Thread.currentThread().getId(), this.releaseTime);
        // 判断结果
        return result != null && result.intValue() == 1;
    }

    @Override
    public void unlock() {
        // 执行脚本
        redisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(key),
                ID_PREFIX + Thread.currentThread().getId(), this.releaseTime);
    }
}