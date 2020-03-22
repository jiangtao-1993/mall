package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringCloudApplication
@EnableZuulProxy
@EnableFeignClients //feign的开关机制
@EnableScheduling //打开任务调度支持
public class LyGateway {
    public static void main(String[] args) {
        SpringApplication.run(LyGateway.class, args);
    }
}