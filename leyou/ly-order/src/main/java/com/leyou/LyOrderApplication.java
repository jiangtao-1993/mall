package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.leyou.order.mapper")
@EnableScheduling //打开定时任务的支持开关
public class LyOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LyOrderApplication.class, args);
    }
}