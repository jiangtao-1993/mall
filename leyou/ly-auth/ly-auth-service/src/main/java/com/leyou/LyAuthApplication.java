package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.leyou.auth.mapper")
@EnableScheduling //打开当前服务的调度功能
public class LyAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyAuthApplication.class);
    }
}
