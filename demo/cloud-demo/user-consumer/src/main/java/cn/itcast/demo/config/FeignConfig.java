package cn.itcast.demo.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;

        //4个级别，FULL，打印输出所有，NONE，表示不输出，默认none
    }
}