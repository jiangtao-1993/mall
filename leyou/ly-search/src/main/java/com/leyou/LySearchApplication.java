package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LySearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(LySearchApplication.class);
    }
}
