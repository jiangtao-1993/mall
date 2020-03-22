package com.leyou.order.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedisConfig {
    //RedisProperties这个类会自动关联配置文件,我们只需要在yml中配置即可
    //为什么不用RedisTemplate这个模块需要redis实现分布式锁的功能
    @Bean
    public RedissonClient redissonClient(RedisProperties prop) {
        String address = "redis://%s:%d";
        Config config = new Config();
        config.useSingleServer()
                .setAddress(String.format(address, prop.getHost(), prop.getPort()));
        return Redisson.create(config);
    }
}