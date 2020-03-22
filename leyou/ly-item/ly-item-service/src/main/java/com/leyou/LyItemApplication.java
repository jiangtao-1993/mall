package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.leyou.item.mapper")
public class LyItemApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyItemApplication.class);
    }
}
